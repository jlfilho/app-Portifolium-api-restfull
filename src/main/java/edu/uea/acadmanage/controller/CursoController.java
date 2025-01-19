package edu.uea.acadmanage.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.DTO.CursoDTO;
import edu.uea.acadmanage.DTO.PermissaoCursoDTO;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.service.CursoService;
import edu.uea.acadmanage.service.UsuarioService;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;
    private final UsuarioService usuarioService;

    public CursoController(CursoService cursoService, UsuarioService usuarioService) {
        this.cursoService = cursoService;
        this.usuarioService = usuarioService;
    }

    // Endpoint para buscar todos os cursos
    @GetMapping
    public ResponseEntity<List<CursoDTO>> buscarTodosCursos() {
        List<CursoDTO> cursos = cursoService.getAllCursos();
        if (cursos.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retorna 204 No Content se não houver cursos
        }
        return ResponseEntity.ok(cursos); // Retorna 200 OK com a lista de cursos
    }

    @GetMapping("/{cursoId}")
    public ResponseEntity<CursoDTO> getCursoById(@PathVariable Long cursoId) {
        CursoDTO curso = cursoService.getCursoById(cursoId);
        return ResponseEntity.ok(curso); // 200 OK se encontrado
    }

    @GetMapping("/permissoes/{cursoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<List<PermissaoCursoDTO>> getAllUsuarioByCurso(@PathVariable Long cursoId, @AuthenticationPrincipal Usuario userDetails) {
        Usuario usuario = usuarioService.getUsuarioByEmail(userDetails.getUsername());
        List<PermissaoCursoDTO> permissaoCursoDTO = cursoService.getAllUsuarioByCurso(cursoId, usuario.getEmail());
        return ResponseEntity.ok(permissaoCursoDTO);
    }

    
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public List<CursoDTO> getCursosByUsuarioId(@AuthenticationPrincipal Usuario userDetails) {
        Usuario usuario = usuarioService.getUsuarioByEmail(userDetails.getUsername());

        return cursoService.getCursosByUsuarioId(usuario.getId());
    }

    // Endpoint para salvar um curso
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CursoDTO> salvarCurso(@Validated @RequestBody CursoDTO cursoDTO, @AuthenticationPrincipal Usuario userDetails) {
        Usuario usuario = usuarioService.getUsuarioByEmail(userDetails.getUsername());
        CursoDTO cursoSalvo = cursoService.saveCurso(cursoDTO,usuario);
        return ResponseEntity.status(201).body(cursoSalvo); // 201 Created
    }

    // Endpoint para atualizar um curso
    @PutMapping("/{cursoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CursoDTO> atualizarCurso(
            @PathVariable Long cursoId,
            @Validated @RequestBody CursoDTO cursoDTO) {
        CursoDTO cursoAtualizado = cursoService.updateCurso(cursoId, cursoDTO);
        return ResponseEntity.ok(cursoAtualizado); // Retorna 200 OK com o curso atualizado
    }

    // Endpoint para atualizar um curso
    @PutMapping("/{cursoId}/status")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CursoDTO> atualizarStatusCurso(
            @PathVariable Long cursoId,
            @Validated @RequestBody CursoDTO cursoDTO) {
        CursoDTO cursoAtualizado = cursoService.updateStatusCurso(cursoId, cursoDTO.ativo());
        return ResponseEntity.ok(cursoAtualizado); // Retorna 200 OK com o curso atualizado
    }

    // Endpoint para excluir um curso
    @PutMapping("/{cursoId}/usuarios/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE')")
    public ResponseEntity<List<PermissaoCursoDTO>> adicionarUsuarioCurso(@PathVariable Long cursoId, 
    @PathVariable Long usuarioId) {
        List<PermissaoCursoDTO> permissaoCurso = cursoService.adicionarUsuarioCurso(cursoId, usuarioId);
        return ResponseEntity.ok(permissaoCurso); // Retorna 204 No Content
    }

    // Endpoint para excluir um curso
    @DeleteMapping("/{cursoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> excluirCurso(@PathVariable Long cursoId) {
        cursoService.excluirCurso(cursoId);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }

    // Endpoint para excluir um curso
    @DeleteMapping("/{cursoId}/usuarios/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE')")
    public ResponseEntity<List<PermissaoCursoDTO>> excluirUsuarioCurso(@PathVariable Long cursoId, 
    @PathVariable Long usuarioId) {
        List<PermissaoCursoDTO> permissaoCurso =  cursoService.removerUsuarioCurso(cursoId, usuarioId);
        return ResponseEntity.ok(permissaoCurso); // Retorna 204 No Content
    }
}
