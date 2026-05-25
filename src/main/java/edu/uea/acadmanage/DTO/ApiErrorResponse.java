package edu.uea.acadmanage.DTO;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int statusCode,
        String status,
        String error,
        String message,
        String path,
        List<String> details,
        String action
) {}
