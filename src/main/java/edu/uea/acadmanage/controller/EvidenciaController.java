package edu.uea.acadmanage.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

import edu.uea.acadmanage.DTO.EvidenciaDTO;
import edu.uea.acadmanage.DTO.EvidenciaOrdemDTO;
import edu.uea.acadmanage.service.EvidenciaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evidencias")
public class EvidenciaController {

    private final EvidenciaService evidenciaService;


    public EvidenciaController(
        EvidenciaService evidenciaService
        ) {
        this.evidenciaService = evidenciaService;
    }

    // Endpoint para listar evidências por atividade
    @GetMapping("/atividade/{atividadeId}")
    public ResponseEntity<List<EvidenciaDTO>> listarEvidenciasPorAtividade(@PathVariable Long atividadeId) {
        List<EvidenciaDTO> evidencias = evidenciaService.listarEvidenciasPorAtividade(atividadeId);
        return evidencias.isEmpty()
                ? ResponseEntity.noContent().build() // 204 No Content
                : ResponseEntity.ok(evidencias); // 200 OK
    }

    // Endpoint para buscar uma evidências por id
    @GetMapping("/{evidenciaId}")
    public ResponseEntity<EvidenciaDTO> getEvidenciasPorId(@PathVariable Long evidenciaId) {
        EvidenciaDTO evidencia = evidenciaService.getEvidenciasPorId(evidenciaId);
        return ResponseEntity.ok(evidencia); // 200 OK
    }

    // Endpoint para salvar uma evidência
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<EvidenciaDTO> salvarEvidencia(
        @RequestParam("atividadeId") Long atividadeId,
        @RequestParam("legenda") String legenda,
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        EvidenciaDTO evidenciaSalva = evidenciaService.salvarEvidencia(atividadeId, legenda, file, userDetails.getUsername());
        return ResponseEntity.status(201).body(evidenciaSalva); // 201 Created
    }

    // Endpoint para atualizar uma evidência    
    @PutMapping(value = "/{evidenciaId}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<EvidenciaDTO> atualizarEvidencia(
            @PathVariable Long evidenciaId,
            @RequestParam("legenda") String legenda,
            @RequestParam("file") MultipartFile file, 
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        EvidenciaDTO evidenciaAtualizada = evidenciaService.atualizarEvidencia(evidenciaId, legenda, file, userDetails.getUsername());
        return ResponseEntity.ok(evidenciaAtualizada); // Retorna 200 OK com a evidência atualizada
    }

    // Endpoint para excluir uma evidência
    @DeleteMapping("/{evidenciaId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<Void> excluirEvidencia(@PathVariable Long evidenciaId, @AuthenticationPrincipal UserDetails userDetails) {
        evidenciaService.excluirEvidencia(evidenciaId, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PutMapping("/atividade/{atividadeId}/ordem")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<List<EvidenciaDTO>> atualizarOrdem(
            @PathVariable Long atividadeId,
            @RequestBody List<@Valid EvidenciaOrdemDTO> ordens,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<EvidenciaDTO> evidenciasAtualizadas = evidenciaService.atualizarOrdemEvidencias(atividadeId, ordens, userDetails.getUsername());
        return ResponseEntity.ok(evidenciasAtualizadas);
    }
}
