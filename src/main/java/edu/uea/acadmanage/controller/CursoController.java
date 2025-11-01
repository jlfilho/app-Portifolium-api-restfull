package edu.uea.acadmanage.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    // Endpoint para buscar todos os cursos com paginação e filtros por status e nome
    @GetMapping
    public ResponseEntity<Page<CursoDTO>> buscarTodosCursos(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long tipoId,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<CursoDTO> cursos = cursoService.getAllCursosPaginadoComFiltros(ativo, nome, tipoId, pageable);
        if (cursos.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retorna 204 No Content se não houver cursos
        }
        return ResponseEntity.ok(cursos); // Retorna 200 OK com a página de cursos
    }

    // Endpoint para buscar um curso por ID
    @GetMapping("/{cursoId}")
    public ResponseEntity<CursoDTO> getCursoById(@PathVariable Long cursoId) {
        CursoDTO curso = cursoService.getCursoById(cursoId);
        return ResponseEntity.ok(curso); // 200 OK se encontrado
    }

    // Endpoint para buscar todos os usuários e suas permissões associados a um curso
    @GetMapping("/permissoes/{cursoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<List<PermissaoCursoDTO>> getAllUsuarioByCurso(@PathVariable Long cursoId, @AuthenticationPrincipal Usuario userDetails) {
        Usuario usuario = usuarioService.getUsuarioByEmail(userDetails.getUsername());
        List<PermissaoCursoDTO> permissaoCursoDTO = cursoService.getAllUsuarioByCurso(cursoId, usuario.getEmail());
        return ResponseEntity.ok(permissaoCursoDTO);
    }

    
    // Endpoint para buscar todos os cursos associados a um usuário com paginação e filtros
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<Page<CursoDTO>> getCursosByUsuarioId(
            @AuthenticationPrincipal Usuario userDetails,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long tipoId,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Usuario usuario = usuarioService.getUsuarioByEmail(userDetails.getUsername());
        Page<CursoDTO> cursos = cursoService.getCursosByUsuarioIdPaginadoComFiltros(usuario.getId(), ativo, nome, tipoId, pageable);
        
        if (cursos.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retorna 204 No Content se não houver cursos
        }
        return ResponseEntity.ok(cursos); // Retorna 200 OK com a página de cursos
    }

    // Endpoint para criar um novo curso
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CursoDTO> salvarCurso(@Validated @RequestBody CursoDTO cursoDTO, @AuthenticationPrincipal Usuario userDetails) {
        Usuario usuario = usuarioService.getUsuarioByEmail(userDetails.getUsername());
        CursoDTO cursoSalvo = cursoService.saveCurso(cursoDTO,usuario);
        return ResponseEntity.status(201).body(cursoSalvo); // 201 Created
    }

    // Endpoint para atualizar os dados de um curso
    @PutMapping("/{cursoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CursoDTO> atualizarCurso(
            @PathVariable Long cursoId,
            @Validated @RequestBody CursoDTO cursoDTO) {
        CursoDTO cursoAtualizado = cursoService.updateCurso(cursoId, cursoDTO);
        return ResponseEntity.ok(cursoAtualizado); // Retorna 200 OK com o curso atualizado
    }

    // Endpoint para atualizar o status de um curso
    @PutMapping("/{cursoId}/status")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CursoDTO> atualizarStatusCurso(
            @PathVariable Long cursoId,
            @Validated @RequestBody CursoDTO cursoDTO) {
        CursoDTO cursoAtualizado = cursoService.updateStatusCurso(cursoId, cursoDTO.ativo());
        return ResponseEntity.ok(cursoAtualizado); // Retorna 200 OK com o curso atualizado
    }

    // Endpoint para adicionar um usuário a um curso
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

    // Endpoint para excluirUsuarioCurso de um curso
    @DeleteMapping("/{cursoId}/usuarios/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE')")
    public ResponseEntity<List<PermissaoCursoDTO>> excluirUsuarioCurso(@PathVariable Long cursoId, 
    @PathVariable Long usuarioId) {
        List<PermissaoCursoDTO> permissaoCurso =  cursoService.removerUsuarioCurso(cursoId, usuarioId);
        return ResponseEntity.ok(permissaoCurso); // Retorna 204 No Content
    }

    // Endpoint para salvar foto de capa
    @PutMapping(value = "/foto-capa/{cursoId}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<CursoDTO> salvarFotoCapa(
            @PathVariable Long cursoId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        CursoDTO cursoSalvo = cursoService.atualizarFotoCapa(cursoId, file, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(cursoSalvo);
    }

    // Endpoint para baixar foto de capa
    @GetMapping("/foto-capa/{cursoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<Resource> downloadFotoCapa(
            @PathVariable Long cursoId,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        Resource resource = cursoService.downloadFotoCapa(cursoId, userDetails.getUsername());
        
        String contentType = "application/octet-stream";
        String filename = resource.getFilename();
        if (filename != null) {
            String lowerFilename = filename.toLowerCase();
            if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lowerFilename.endsWith(".png")) {
                contentType = "image/png";
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + (filename != null ? filename : "foto-capa") + "\"")
                .body(resource);
    }
}
