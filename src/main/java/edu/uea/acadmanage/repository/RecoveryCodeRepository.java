package edu.uea.acadmanage.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import edu.uea.acadmanage.model.RecoveryCode;

@Repository
public interface RecoveryCodeRepository extends JpaRepository<RecoveryCode, Long> {

    Optional<RecoveryCode> findByCodeAndExpirationTimeAfter(String code, LocalDateTime currentTime);

    @Modifying
    @Query("DELETE FROM RecoveryCode rc WHERE rc.expirationTime < :currentTime")
    void deleteExpiredCodes(LocalDateTime currentTime);

    void deleteByCode(String code);
}
