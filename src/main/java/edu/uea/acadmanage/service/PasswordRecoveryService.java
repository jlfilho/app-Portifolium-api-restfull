package edu.uea.acadmanage.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.uea.acadmanage.model.RecoveryCode;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.RecoveryCodeRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;

@Service
public class PasswordRecoveryService {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final RecoveryCodeRepository recoveryCodeRepository;

    public PasswordRecoveryService(
            UsuarioRepository usuarioRepository,
            EmailService emailService,
            RecoveryCodeRepository recoveryCodeRepository) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.recoveryCodeRepository = recoveryCodeRepository;
    }

    @Transactional
    public void generateRecoveryCode(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // Gerar código único
        String recoveryCode = UUID.randomUUID().toString();

        // Configurar expiração em 30 minutos
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(30);

        // Persistir código
        RecoveryCode codeEntity = new RecoveryCode();
        codeEntity.setCode(recoveryCode);
        codeEntity.setExpirationTime(expirationTime);
        codeEntity.setUsuario(usuario);

        recoveryCodeRepository.save(codeEntity);

        // Enviar email com o código
        String subject = "Código de Recuperação";
        String message = String.format(
                "Olá, %s! Use este código para recuperar sua senha: %s\nO código expira em 30 minutos.",
                usuario.getPessoa().getNome(), recoveryCode);

        emailService.sendSimpleEmail(email, subject, message);
    }

    @Transactional
    public void resetPassword(String email, String recoveryCode, String newPassword) {
        // Verificar se o código é válido
        if (!validateRecoveryCode(recoveryCode)) {
            throw new AcessoNegadoException("Código de recuperação inválido ou expirado");
        }

        // Redefinir a senha
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AcessoNegadoException("Usuário com email " + email + " não encontrado"));
                
        usuario.setSenha(new BCryptPasswordEncoder().encode(newPassword));
        usuarioRepository.save(usuario);

        // Remover o código de recuperação após o uso
        recoveryCodeRepository.deleteByCode(recoveryCode);
    }

    // Verifica o código de recuperação
    @Transactional(readOnly = true)
    public boolean validateRecoveryCode(String recoveryCode) {
        return recoveryCodeRepository.findByCodeAndExpirationTimeAfter(recoveryCode, LocalDateTime.now()).isPresent();
    }

    // Remove códigos expirados
    @Transactional
    public void cleanUpExpiredCodes() {
        recoveryCodeRepository.deleteExpiredCodes(LocalDateTime.now());
    }
}
