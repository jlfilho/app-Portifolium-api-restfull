package edu.uea.acadmanage.controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.model.TipoCurso;
import edu.uea.acadmanage.service.TipoCursoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tipos-curso")
@Validated
public class TipoCursoController {

    private final TipoCursoService tipoCursoService;

    public TipoCursoController(TipoCursoService tipoCursoService) {
        this.tipoCursoService = tipoCursoService;
    }

    @GetMapping
    public ResponseEntity<Page<TipoCurso>> listarTodos(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<TipoCurso> tipos = tipoCursoService.listarPaginadoComFiltro(nome, pageable);
        if (tipos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoCurso> recuperarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoCursoService.recuperarPorId(id));
    }

    // Removido endpoint por código; tipos são gerenciados por ID ou nome no serviço

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoCurso> salvar(@Valid @RequestBody TipoCurso tipoCurso) {
        TipoCurso novo = tipoCursoService.salvar(tipoCurso);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoCurso> atualizar(@PathVariable Long id, @Valid @RequestBody TipoCurso tipoCurso) {
        return ResponseEntity.ok(tipoCursoService.atualizar(id, tipoCurso));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        tipoCursoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}


