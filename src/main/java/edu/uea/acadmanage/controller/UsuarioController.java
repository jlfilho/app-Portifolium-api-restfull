package edu.uea.acadmanage.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.DTO.AuthorityCheckDTO;
import edu.uea.acadmanage.DTO.PasswordChangeRequest;
import edu.uea.acadmanage.DTO.UsuarioDTO;
import edu.uea.acadmanage.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Método para verificar as autoridades do usuário autenticado
    @GetMapping("/checkAuthorities")
    public ResponseEntity<AuthorityCheckDTO> checkAuthorities() {
        // Obtém o contexto de segurança atual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Coleta as authorities atribuídas ao usuário autenticado
        List<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        AuthorityCheckDTO response = new AuthorityCheckDTO(
            authentication.getName(),
            authorities
        );

        return ResponseEntity.ok(response);
    }

    // Método para listar todos os usuários com paginação
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<UsuarioDTO>> listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<UsuarioDTO> usuarios = usuarioService.getAllUsuariosPaginados(pageable);
        return ResponseEntity.ok(usuarios);
    }

    // Método para buscar um único usuário por ID
    @GetMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<UsuarioDTO> getUsuarioById(@PathVariable Long usuarioId) {
        UsuarioDTO usuario = usuarioService.getUsuarioById(usuarioId);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioDTO> criarUsuario(@Validated @RequestBody UsuarioDTO usuario) {
        UsuarioDTO novoUsuario = usuarioService.save(usuario);
        return ResponseEntity.status(201).body(novoUsuario); // 201 Created
    }

    @PutMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<UsuarioDTO> atualizarUsuario(@PathVariable Long usuarioId, 
    @Validated @RequestBody UsuarioDTO usuario) {
        UsuarioDTO novoUsuario = usuarioService.update(usuarioId, usuario);
        return ResponseEntity.ok(novoUsuario);  
    }

    
    @PutMapping("/{usuarioId}/change-password")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long usuarioId,
            @RequestBody @Validated PasswordChangeRequest passwordChangeRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        usuarioService.changePassword(usuarioId, passwordChangeRequest, userDetails.getUsername());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Senha alterada com sucesso");
        response.put("usuarioId", usuarioId.toString());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // Apenas administradores podem excluir usuários
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long usuarioId) {
            usuarioService.deleteUsuario(usuarioId);
            return ResponseEntity.ok().build();
    }
    
}
