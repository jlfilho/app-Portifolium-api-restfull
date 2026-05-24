package edu.uea.acadmanage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_log", indexes = {
    @Index(name = "idx_action_user", columnList = "user_email"),
    @Index(name = "idx_action_type", columnList = "action_type"),
    @Index(name = "idx_action_timestamp", columnList = "timestamp")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActionType actionType;
    
    @Column(nullable = false, length = 100)
    private String userEmail;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 45)
    private String ipAddress;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 500)
    private String endpoint;
    
    @Column(nullable = false)
    private Boolean success;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON com informações adicionais
    
    public enum ActionType {
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        PASSWORD_CHANGE,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_COMPLETE,
        FILE_UPLOAD,
        FILE_DOWNLOAD,
        FILE_DELETE,
        EXPORT_REPORT,
        IMPORT_CSV,
        ACCESS_DENIED,
        PERMISSION_CHECK_FAILED,
        TOKEN_REFRESH,
        SESSION_EXPIRED
    }
}

