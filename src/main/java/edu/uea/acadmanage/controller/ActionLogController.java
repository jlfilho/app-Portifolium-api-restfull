package edu.uea.acadmanage.controller;

import edu.uea.acadmanage.DTO.ActionLogFilterDTO;
import edu.uea.acadmanage.model.ActionLog;
import edu.uea.acadmanage.repository.ActionLogRepository;
import edu.uea.acadmanage.service.ActionLogExportService;
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

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/action-logs")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ActionLogController {
    
    private final ActionLogRepository actionLogRepository;
    private final ActionLogExportService exportService;

    public ActionLogController(ActionLogRepository actionLogRepository,
                              ActionLogExportService exportService) {
        this.actionLogRepository = actionLogRepository;
        this.exportService = exportService;
    }

    @GetMapping
    public ResponseEntity<Page<ActionLog>> getAllLogs(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(actionLogRepository.findAll(pageable));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<Page<ActionLog>> getLogsByUser(
            @PathVariable String email,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(actionLogRepository.findByUserEmail(email, pageable));
    }

    @GetMapping("/type/{actionType}")
    public ResponseEntity<Page<ActionLog>> getLogsByType(
            @PathVariable ActionLog.ActionType actionType,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(actionLogRepository.findByActionType(actionType, pageable));
    }

    @GetMapping("/success/{success}")
    public ResponseEntity<Page<ActionLog>> getLogsBySuccess(
            @PathVariable Boolean success,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(actionLogRepository.findBySuccess(success, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<ActionLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(actionLogRepository.findByDateRange(startDate, endDate, pageable));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<StreamingResponseBody> exportToCsv(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) ActionLog.ActionType actionType,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        ActionLogFilterDTO filter = new ActionLogFilterDTO(
            userEmail,
            actionType,
            success,
            startDate,
            endDate
        );
        
        StreamingResponseBody stream = outputStream -> {
            try {
                exportService.exportToCsv(filter, outputStream);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao exportar logs de ação", e);
            }
        };
        
        String filename = "action-logs-" + LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv";
        
        return ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
            .body(stream);
    }
}

