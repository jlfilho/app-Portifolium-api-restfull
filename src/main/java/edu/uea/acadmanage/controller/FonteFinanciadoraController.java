package edu.uea.acadmanage.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.model.FonteFinanciadora;
import edu.uea.acadmanage.service.FonteFinanciadoraService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fontes-financiadoras")
public class FonteFinanciadoraController {

    private final FonteFinanciadoraService fonteFinanciadoraService;

    public FonteFinanciadoraController(FonteFinanciadoraService fonteFinanciadoraService) {
        this.fonteFinanciadoraService = fonteFinanciadoraService;
    }

    // Endpoint para listar todas as fontes financiadoras
    @GetMapping
    public ResponseEntity<List<FonteFinanciadora>> listarTodas() {
        List<FonteFinanciadora> fontes = fonteFinanciadoraService.listarTodasFontesFinanciadoras();
        return fontes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(fontes);
    }

    // Endpoint para recuperar uma fonte financiadora pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<FonteFinanciadora> recuperarPorId(@PathVariable Long id) {
        FonteFinanciadora fonteFinanciadora = fonteFinanciadoraService.recuperarFinanciadoraPorId(id);
        return ResponseEntity.ok(fonteFinanciadora);
    }

    // Endpoint para criar uma nova fonte financiadora
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<FonteFinanciadora> salvar(@Valid @RequestBody FonteFinanciadora fonteFinanciadora) {
        FonteFinanciadora novaFonte = fonteFinanciadoraService.salvar(fonteFinanciadora);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaFonte);
    }

    // Endpoint para atualizar uma fonte financiadora
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<FonteFinanciadora> atualizar(@PathVariable Long id, @Valid @RequestBody FonteFinanciadora fonteFinanciadora) {
        FonteFinanciadora fonteAtualizada = fonteFinanciadoraService.atualizar(id, fonteFinanciadora);
        return ResponseEntity.ok(fonteAtualizada);
    }

    // Endpoint para deletar uma fonte financiadora
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        fonteFinanciadoraService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
