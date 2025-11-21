package edu.uea.acadmanage.repository;

import edu.uea.acadmanage.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);
    
    Page<AuditLog> findByUserEmail(String userEmail, Pageable pageable);
    
    Page<AuditLog> findByAction(AuditLog.AuditAction action, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate, 
                                    Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.entityName = :entityName " +
           "AND a.entityId = :entityId ORDER BY a.timestamp DESC")
    List<AuditLog> findHistoryByEntity(@Param("entityName") String entityName, 
                                       @Param("entityId") Long entityId);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userEmail = :userEmail " +
           "AND a.action = :action AND a.timestamp >= :since")
    Long countByUserAndActionSince(@Param("userEmail") String userEmail,
                                    @Param("action") AuditLog.AuditAction action,
                                    @Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userEmail IS NULL OR LOWER(a.userEmail) LIKE LOWER(CONCAT('%', :userEmail, '%'))) " +
           "AND (:action IS NULL OR a.action = :action) " +
           "AND (:entityName IS NULL OR LOWER(a.entityName) LIKE LOWER(CONCAT('%', :entityName, '%'))) " +
           "AND (:entityId IS NULL OR a.entityId = :entityId) " +
           "AND (:startDate IS NULL OR a.timestamp >= :startDate) " +
           "AND (:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findWithFilters(@Param("userEmail") String userEmail,
                                    @Param("action") AuditLog.AuditAction action,
                                    @Param("entityName") String entityName,
                                    @Param("entityId") Long entityId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
}

