package edu.uea.acadmanage.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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
    private final String frontendUrl;

    public PasswordRecoveryService(
            UsuarioRepository usuarioRepository,
            EmailService emailService,
            RecoveryCodeRepository recoveryCodeRepository,
            @Value("${app.frontend.url:http://localhost:4200}") String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.recoveryCodeRepository = recoveryCodeRepository;
        this.frontendUrl = frontendUrl;
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

        // Enviar email com o código e link
        // Se o envio falhar, a exceção será propagada e a transação será revertida
        // (o código não será salvo no banco)
        String subject = "Código de Recuperação de Senha";
        
        String message = String.format(
                "Olá, %s!\n\n" +
                "Você solicitou a recuperação de senha. Use o código abaixo ou clique no link para redefinir sua senha:\n\n" +
                "Código: %s\n\n" +
                "O código expira em 30 minutos.\n\n" +
                "Se você não solicitou esta recuperação, ignore este email.",
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
