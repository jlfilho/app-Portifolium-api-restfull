package edu.uea.acadmanage.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.model.AtividadePessoaId;
import edu.uea.acadmanage.model.AtividadePessoaPapel;
import edu.uea.acadmanage.model.Papel;
import edu.uea.acadmanage.service.AtividadePessoaPapelService;

@RestController
@RequestMapping("/api/atividades-pessoas")
public class AtividadePessoaPapelController {

    private final AtividadePessoaPapelService atividadePessoaPapelService;

    public AtividadePessoaPapelController(AtividadePessoaPapelService atividadePessoaPapelService) {
        this.atividadePessoaPapelService = atividadePessoaPapelService;
    }

    @PostMapping("/{atividadeId}/pessoas/{pessoaId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<AtividadePessoaPapel> associarPessoa(
            @PathVariable Long atividadeId,
            @PathVariable Long pessoaId,
            @RequestParam Papel papel,
            @AuthenticationPrincipal UserDetails userDetails) {
        AtividadePessoaPapel associacao = atividadePessoaPapelService.associarPessoa(atividadeId, pessoaId, papel, userDetails.getUsername());
        return ResponseEntity.status(201).body(associacao); // 201 Created
    }

    @PutMapping("/{atividadeId}/pessoas/{pessoaId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<AtividadePessoaPapel> alterarPapel(
            @PathVariable Long atividadeId,
            @PathVariable Long pessoaId,
            @RequestParam Papel novoPapel,
            @AuthenticationPrincipal UserDetails userDetails) {
        AtividadePessoaId associacaoId = new AtividadePessoaId(atividadeId, pessoaId);
        AtividadePessoaPapel atualizada = atividadePessoaPapelService.alterarPapel(associacaoId, novoPapel, userDetails.getUsername());
        return ResponseEntity.ok(atualizada); // 200 OK
    }

    @GetMapping("/{atividadeId}/pessoas")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<List<AtividadePessoaPapel>> listarPessoasPorAtividade(@PathVariable Long atividadeId) {
        List<AtividadePessoaPapel> pessoas = atividadePessoaPapelService.listarPorAtividade(atividadeId);
        return ResponseEntity.ok(pessoas); // 200 OK
    }

    @DeleteMapping("/{atividadeId}/pessoas/{pessoaId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<Void> removerPessoaDaAtividade(
            @PathVariable Long atividadeId,
            @PathVariable Long pessoaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        atividadePessoaPapelService.removerPessoaDaAtividade(atividadeId, pessoaId, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}

