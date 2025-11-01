package edu.uea.acadmanage.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.model.TipoCurso;
import edu.uea.acadmanage.model.TipoCursoCodigo;
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
    public ResponseEntity<List<TipoCurso>> listarTodos() {
        List<TipoCurso> tipos = tipoCursoService.listarTodos();
        return tipos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(tipos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoCurso> recuperarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoCursoService.recuperarPorId(id));
    }

    @GetMapping("/por-codigo")
    public ResponseEntity<TipoCurso> recuperarPorCodigo(@RequestParam("codigo") TipoCursoCodigo codigo) {
        return ResponseEntity.ok(tipoCursoService.recuperarPorCodigo(codigo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<TipoCurso> salvar(@Valid @RequestBody TipoCurso tipoCurso) {
        TipoCurso novo = tipoCursoService.salvar(tipoCurso);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<TipoCurso> atualizar(@PathVariable Long id, @Valid @RequestBody TipoCurso tipoCurso) {
        return ResponseEntity.ok(tipoCursoService.atualizar(id, tipoCurso));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        tipoCursoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}


