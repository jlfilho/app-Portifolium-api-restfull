package edu.uea.acadmanage.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        if (!StringUtils.hasText(email)) {
            return ResponseEntity.badRequest().body("Email é obrigatório");
        }
        try {
            passwordRecoveryService.generateRecoveryCode(email);
            return ResponseEntity.ok("Código de recuperação enviado para o email.");
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body("Usuário não encontrado com o email fornecido.");
        } catch (edu.uea.acadmanage.service.exception.ErroEnvioEmailException e) {
            // Em ambiente de teste, podemos continuar mesmo se o email falhar
            // O código já foi gerado, apenas o envio falhou
            return ResponseEntity.ok("Código de recuperação gerado. Nota: Falha ao enviar email.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String recoveryCode,
            @RequestParam(required = false) String newPassword,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        // Aceita tanto query parameters quanto body JSON
        String emailToUse = email;
        String codeToUse = recoveryCode;
        String passwordToUse = newPassword;
        
        if (requestBody != null) {
            if (!StringUtils.hasText(emailToUse)) {
                emailToUse = requestBody.get("email");
            }
            if (!StringUtils.hasText(codeToUse)) {
                codeToUse = requestBody.get("recoveryCode");
            }
            if (!StringUtils.hasText(passwordToUse)) {
                passwordToUse = requestBody.get("newPassword");
            }
        }
        
        if (!StringUtils.hasText(emailToUse) || !StringUtils.hasText(codeToUse) || !StringUtils.hasText(passwordToUse)) {
            return ResponseEntity.badRequest().body("Email, código de recuperação e nova senha são obrigatórios");
        }
        
        try {
            passwordRecoveryService.resetPassword(emailToUse, codeToUse, passwordToUse);
            return ResponseEntity.ok("Senha redefinida com sucesso");
        } catch (edu.uea.acadmanage.service.exception.AcessoNegadoException e) {
            // Já tratado pelo GlobalExceptionHandler, mas garantimos que retorna o erro correto
            throw e;
        }
    }
}
