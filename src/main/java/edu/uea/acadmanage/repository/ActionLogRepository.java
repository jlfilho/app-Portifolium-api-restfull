package edu.uea.acadmanage.repository;

import edu.uea.acadmanage.model.ActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
    
    Page<ActionLog> findByUserEmail(String userEmail, Pageable pageable);
    
    Page<ActionLog> findByActionType(ActionLog.ActionType actionType, Pageable pageable);
    
    Page<ActionLog> findBySuccess(Boolean success, Pageable pageable);
    
    @Query("SELECT a FROM ActionLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<ActionLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate, 
                                    Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM ActionLog a WHERE a.userEmail = :userEmail " +
           "AND a.actionType = :actionType AND a.success = false " +
           "AND a.timestamp >= :since")
    Long countFailedActionsByUser(@Param("userEmail") String userEmail,
                                  @Param("actionType") ActionLog.ActionType actionType,
                                  @Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM ActionLog a WHERE " +
           "(:userEmail IS NULL OR LOWER(a.userEmail) LIKE LOWER(CONCAT('%', :userEmail, '%'))) " +
           "AND (:actionType IS NULL OR a.actionType = :actionType) " +
           "AND (:success IS NULL OR a.success = :success) " +
           "AND (:startDate IS NULL OR a.timestamp >= :startDate) " +
           "AND (:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    List<ActionLog> findWithFilters(@Param("userEmail") String userEmail,
                                    @Param("actionType") ActionLog.ActionType actionType,
                                    @Param("success") Boolean success,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
}

