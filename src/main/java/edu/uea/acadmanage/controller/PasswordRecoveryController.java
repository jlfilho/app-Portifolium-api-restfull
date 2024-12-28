package edu.uea.acadmanage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.service.PasswordRecoveryService;

@RestController
@RequestMapping("/api/recovery")
public class PasswordRecoveryController {

    private final PasswordRecoveryService passwordRecoveryService;

    public PasswordRecoveryController(PasswordRecoveryService passwordRecoveryService) {
        this.passwordRecoveryService = passwordRecoveryService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateRecoveryCode(@RequestParam String email) {
        passwordRecoveryService.generateRecoveryCode(email);
        return ResponseEntity.ok("Código de recuperação enviado para o email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email,
                                                 @RequestParam String recoveryCode,
                                                 @RequestParam String newPassword) {
        passwordRecoveryService.resetPassword(email, recoveryCode, newPassword);
        return ResponseEntity.ok("Senha redefinida com sucesso");
    }
}
