package edu.uea.acadmanage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_name,entity_id"),
    @Index(name = "idx_audit_user", columnList = "user_email"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String entityName; // Ex: "Curso", "Atividade", "Usuario"
    
    @Column(nullable = false)
    private Long entityId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;
    
    @Column(nullable = false, length = 100)
    private String userEmail;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(columnDefinition = "TEXT")
    private String oldValues; // JSON com valores antigos
    
    @Column(columnDefinition = "TEXT")
    private String newValues; // JSON com valores novos
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 45)
    private String ipAddress;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 500)
    private String endpoint; // Ex: "/api/cursos"
    
    @Column(length = 10)
    private String httpMethod; // Ex: "POST", "PUT", "DELETE"
    
    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE,
        VIEW,
        EXPORT,
        LOGIN,
        LOGOUT,
        PASSWORD_CHANGE,
        UPLOAD_FILE,
        DOWNLOAD_FILE,
        ACCESS_DENIED
    }
}

