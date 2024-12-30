package edu.uea.acadmanage.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.DTO.PasswordChangeRequest;
import edu.uea.acadmanage.DTO.UsuarioDTO;
import edu.uea.acadmanage.service.CustomUserDetailsService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final CustomUserDetailsService usuarioService;

    public UsuarioController(CustomUserDetailsService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Método para verificar as autoridades do usuário autenticado
    @GetMapping("/checkAuthorities")
    public void checkAuthorities() {
        // Obtém o contexto de segurança atual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Imprime as authorities atribuídas ao usuário autenticado
        System.out.println("Username: " + authentication.getName());
        System.out.println("Authorities: ");
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            System.out.println(authority.getAuthority());
        }
    }

    // Método para listar todos os usuários
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        List<UsuarioDTO> usuarios = usuarioService.getAllUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioDTO> criarUsuario(@RequestBody UsuarioDTO usuario) {
        UsuarioDTO novoUsuario = usuarioService.save(usuario);
        return ResponseEntity.status(201).body(novoUsuario); // 201 Created
    }

    @PutMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<UsuarioDTO> atualizarUsuario(@PathVariable Long usuarioId, 
    @RequestBody UsuarioDTO usuario) {
        UsuarioDTO novoUsuario = usuarioService.update(usuarioId, usuario);
        return ResponseEntity.status(200).body(novoUsuario);  
    }

    
    @PutMapping("/{usuarioId}/change-password")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<String> changePassword(
            @PathVariable Long usuarioId,
            @RequestBody @Validated PasswordChangeRequest passwordChangeRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        usuarioService.changePassword(usuarioId, passwordChangeRequest, userDetails.getUsername());
        return ResponseEntity.ok("Senha alterada com sucesso");
    }

    @DeleteMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // Apenas administradores podem excluir usuários
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long usuarioId) {
            usuarioService.deleteUsuario(usuarioId);
            return ResponseEntity.noContent().build();
    }
    
}
