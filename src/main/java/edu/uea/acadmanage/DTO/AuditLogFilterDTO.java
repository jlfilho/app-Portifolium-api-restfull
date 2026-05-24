package edu.uea.acadmanage.DTO;

import edu.uea.acadmanage.model.AuditLog;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record AuditLogFilterDTO(
    String userEmail,
    AuditLog.AuditAction action,
    String entityName,
    Long entityId,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime startDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime endDate
) {
}

