package edu.uea.acadmanage.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.DTO.UnidadeAcademicaDTO;
import edu.uea.acadmanage.service.UnidadeAcademicaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/unidades-academicas")
public class UnidadeAcademicaController {

    private final UnidadeAcademicaService unidadeAcademicaService;

    public UnidadeAcademicaController(UnidadeAcademicaService unidadeAcademicaService) {
        this.unidadeAcademicaService = unidadeAcademicaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UnidadeAcademicaDTO> criar(@Valid @RequestBody UnidadeAcademicaDTO dto) {
        UnidadeAcademicaDTO salvo = unidadeAcademicaService.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UnidadeAcademicaDTO> atualizar(@PathVariable Long id,
            @Valid @RequestBody UnidadeAcademicaDTO dto) {
        UnidadeAcademicaDTO atualizado = unidadeAcademicaService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<Page<UnidadeAcademicaDTO>> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<UnidadeAcademicaDTO> page = unidadeAcademicaService.listar(nome, pageable);
        if (page.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<UnidadeAcademicaDTO> buscarPorId(@PathVariable Long id) {
        UnidadeAcademicaDTO dto = unidadeAcademicaService.buscarPorId(id);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        unidadeAcademicaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

