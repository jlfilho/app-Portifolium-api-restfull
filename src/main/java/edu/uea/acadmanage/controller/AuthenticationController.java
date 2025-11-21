package edu.uea.acadmanage.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.DTO.LoginRequestDTO;
import edu.uea.acadmanage.DTO.LoginResponseDTO;
import edu.uea.acadmanage.model.ActionLog;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.service.ActionLogService;
import edu.uea.acadmanage.service.AuthenticationService;
import edu.uea.acadmanage.service.JwtService;

@RequestMapping("/api/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final ActionLogService actionLogService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService,
                                    ActionLogService actionLogService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.actionLogService = actionLogService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticate(@RequestBody LoginRequestDTO loginUserDto) {
        try {
            Usuario authenticatedUser = this.authenticationService.authenticate(loginUserDto);
            
            String jwtToken = jwtService.generateToken(authenticatedUser);
            LoginResponseDTO loginResponse = new LoginResponseDTO(jwtToken, jwtService.getExpirationTime());

            // CAMADA 3: Action Log - Login bem-sucedido
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("roles", authenticatedUser.getRoles().stream()
                .map(r -> r.getNome())
                .toList());
            actionLogService.log(
                ActionLog.ActionType.LOGIN_SUCCESS,
                true,
                "Login realizado com sucesso: " + loginUserDto.username(),
                null,
                metadata
            );

            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            // CAMADA 3: Action Log - Login falhou
            actionLogService.log(
                ActionLog.ActionType.LOGIN_FAILURE,
                false,
                "Tentativa de login falhou: " + loginUserDto.username(),
                e.getMessage(),
                null
            );
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        try {
            // Capturar informações do usuário antes de limpar o contexto
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication != null && authentication.isAuthenticated() 
                ? authentication.getName() 
                : "anonymous";
            
            // CAMADA 3: Action Log - Logout
            actionLogService.log(
                ActionLog.ActionType.LOGOUT,
                true,
                "Logout realizado: " + userEmail,
                null,
                new HashMap<>()
            );
            
            // Limpar o SecurityContext (embora JWT seja stateless, isso garante limpeza no servidor)
            SecurityContextHolder.clearContext();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout realizado com sucesso");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // CAMADA 3: Action Log - Logout falhou
            actionLogService.log(
                ActionLog.ActionType.LOGOUT,
                false,
                "Erro ao realizar logout",
                e.getMessage(),
                new HashMap<>()
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout realizado (com avisos)");
            
            return ResponseEntity.ok(response);
        }
    }
}