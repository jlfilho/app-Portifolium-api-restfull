package edu.uea.acadmanage.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import edu.uea.acadmanage.DTO.CursoDTO;
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

    
    @GetMapping("/usuario")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public List<CursoDTO> getCursosByUsuarioId(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioService.getUsuarioByEmail(userDetails.getUsername());

        return cursoService.getCursosByUsuarioId(usuario.getId());
    }

    // Endpoint para salvar um curso
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CursoDTO> salvarCurso(@Validated @RequestBody CursoDTO cursoDTO) {
        CursoDTO cursoSalvo = cursoService.saveCurso(cursoDTO);
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

    // Endpoint para excluir um curso
    @DeleteMapping("/{cursoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> excluirCurso(@PathVariable Long cursoId) {
        cursoService.excluirCurso(cursoId);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}
