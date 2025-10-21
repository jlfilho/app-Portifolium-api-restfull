package edu.uea.acadmanage.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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

import edu.uea.acadmanage.DTO.AtividadeDTO;
import edu.uea.acadmanage.DTO.AtividadeFiltroDTO;
import edu.uea.acadmanage.service.AtividadeService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/atividades")
public class AtividadeController {

    private final AtividadeService atividadeService;

    public AtividadeController(AtividadeService atividadeService) {
        this.atividadeService = atividadeService;
    }

    // Endpoint para pesquisar atividades por curso
    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<AtividadeDTO>> getAtividadesPorCurso(@PathVariable Long cursoId) {
        List<AtividadeDTO> atividades = atividadeService.getAtividadesPorCurso(cursoId);
        return atividades.isEmpty()
                ? ResponseEntity.noContent().build() // 204 No Content
                : ResponseEntity.ok(atividades); // 200 OK
    }

    // Método para buscar atividades por id
    @GetMapping("/{atividadeId}")
    public ResponseEntity<AtividadeDTO> getAtividadeById(@PathVariable Long atividadeId) {
        AtividadeDTO atividade = atividadeService.getAtividadeById(atividadeId);
        return ResponseEntity.ok(atividade); // 200 OK se encontrado
    }

    // Método para buscar uma atividade por ID e usuário
    @GetMapping("/{atividadeId}/usuario/{usuarioId}")
    public ResponseEntity<AtividadeDTO> getAtividadeByIdAndUsuario(
            @PathVariable Long atividadeId,
            @PathVariable Long usuarioId) {
        AtividadeDTO atividade = atividadeService.getAtividadeByIdAndUsuario(atividadeId, usuarioId);
        return ResponseEntity.ok(atividade); // Retorna 200 OK com a atividade encontrada
    }

    // Endpoint para pesquisar atividades com múltiplos filtros, incluindo
    // statusPublicacao (com paginação)
    @GetMapping("/filtros")
    public ResponseEntity<Page<AtividadeDTO>> getAtividadesPorFiltros(
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Boolean statusPublicacao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        AtividadeFiltroDTO filtros = new AtividadeFiltroDTO(cursoId, categoriaId, nome, dataInicio, dataFim,
                statusPublicacao);

        // Configurar paginação e ordenação
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AtividadeDTO> atividades = atividadeService.getAtividadesPorFiltrosPaginado(filtros, pageable);

        return atividades.isEmpty()
                ? ResponseEntity.noContent().build() // 204 No Content
                : ResponseEntity.ok(atividades); // 200 OK
    }

    // Endpoint para salvar uma atividade
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<AtividadeDTO> salvarAtividade(@Validated @RequestBody AtividadeDTO atividadeDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        AtividadeDTO atividadeSalva = atividadeService.salvarAtividade(atividadeDTO, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(atividadeSalva); // 201 Created
    }

    // Endpoint para salvar foto de capa
    @PutMapping(value = "/foto-capa/{atividadeId}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<AtividadeDTO> salvarFotoCapa(
            @PathVariable Long atividadeId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        System.out.println("atividadeId: " + atividadeId);        
        AtividadeDTO atividadeSalva = atividadeService.atualizarFotoCapa(atividadeId, file, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(atividadeSalva);
    }

    // Endpoint para atualizar uma atividade
    @PutMapping("/{atividadeId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<AtividadeDTO> atualizarAtividade(
            @PathVariable Long atividadeId,
            @Valid @RequestBody AtividadeDTO atividadeDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        AtividadeDTO atividadeAtualizada = atividadeService.atualizarAtividade(atividadeId, atividadeDTO,
                userDetails.getUsername());
        return ResponseEntity.ok(atividadeAtualizada); // Retorna 200 OK com a atividade atualizada
    }

    // Endpoint para excluir uma atividade
    @DeleteMapping(value = "/{atividadeId}/foto-capa")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<Void> excluirFotoCapa(@PathVariable Long atividadeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        atividadeService.excluirFotoCapa(atividadeId, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }

    // Endpoint para excluir uma atividade
    @DeleteMapping("/{atividadeId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<Void> excluirAtividade(@PathVariable Long atividadeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        atividadeService.excluirAtividade(atividadeId, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }

}
