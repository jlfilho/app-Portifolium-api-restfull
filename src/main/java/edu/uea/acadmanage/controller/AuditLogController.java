package edu.uea.acadmanage.controller;

import edu.uea.acadmanage.DTO.AuditLogFilterDTO;
import edu.uea.acadmanage.model.AuditLog;
import edu.uea.acadmanage.repository.AuditLogRepository;
import edu.uea.acadmanage.service.AuditLogExportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AuditLogController {
    
    private final AuditLogRepository auditLogRepository;
    private final AuditLogExportService exportService;

    public AuditLogController(AuditLogRepository auditLogRepository, 
                             AuditLogExportService exportService) {
        this.auditLogRepository = auditLogRepository;
        this.exportService = exportService;
    }

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAllLogs(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditLogRepository.findAll(pageable));
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<Page<AuditLog>> getLogsByEntity(
            @PathVariable String entityName,
            @PathVariable Long entityId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditLogRepository.findByEntityNameAndEntityId(
            entityName, entityId, pageable));
    }

    @GetMapping("/entity/{entityName}/{entityId}/history")
    public ResponseEntity<List<AuditLog>> getHistoryByEntity(
            @PathVariable String entityName,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(auditLogRepository.findHistoryByEntity(entityName, entityId));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<Page<AuditLog>> getLogsByUser(
            @PathVariable String email,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditLogRepository.findByUserEmail(email, pageable));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<Page<AuditLog>> getLogsByAction(
            @PathVariable AuditLog.AuditAction action,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditLogRepository.findByAction(action, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<AuditLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditLogRepository.findByDateRange(startDate, endDate, pageable));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<StreamingResponseBody> exportToCsv(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) AuditLog.AuditAction action,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        AuditLogFilterDTO filter = new AuditLogFilterDTO(
            userEmail,
            action,
            entityName,
            entityId,
            startDate,
            endDate
        );
        
        StreamingResponseBody stream = outputStream -> {
            try {
                exportService.exportToCsv(filter, outputStream);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao exportar logs de auditoria", e);
            }
        };
        
        String filename = "audit-logs-" + LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv";
        
        return ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
            .body(stream);
    }
}

