package edu.uea.acadmanage.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecoveryCodeCleanupTask {

    private final PasswordRecoveryService recoveryService;

    public RecoveryCodeCleanupTask(PasswordRecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    @Scheduled(fixedRate = 3600000) // A cada 1 hora
    public void cleanUpExpiredCodes() {
        recoveryService.cleanUpExpiredCodes();
    }
}
