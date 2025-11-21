package edu.uea.acadmanage.DTO;

import edu.uea.acadmanage.model.ActionLog;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record ActionLogFilterDTO(
    String userEmail,
    ActionLog.ActionType actionType,
    Boolean success,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime startDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime endDate
) {
}

