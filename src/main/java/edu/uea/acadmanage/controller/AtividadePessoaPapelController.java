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
import org.springframework.web.multipart.MultipartFile;

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

    // Método para associar uma pessoa a uma atividade
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

    // Método para alterar o papel de uma pessoa em uma atividade
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

    // Método para listar as pessoas associadas a uma atividade
    @GetMapping("/{atividadeId}/pessoas")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<List<AtividadePessoaPapel>> listarPessoasPorAtividade(@PathVariable Long atividadeId) {
        List<AtividadePessoaPapel> pessoas = atividadePessoaPapelService.listarPorAtividade(atividadeId);
        return ResponseEntity.ok(pessoas); // 200 OK
    }
    
    // Método para remover uma pessoa de uma atividade
    @DeleteMapping("/{atividadeId}/pessoas/{pessoaId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<Void> removerPessoaDaAtividade(
            @PathVariable Long atividadeId,
            @PathVariable Long pessoaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        atividadePessoaPapelService.removerPessoaDaAtividade(atividadeId, pessoaId, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // Método para importar pessoas de um arquivo CSV
    @PostMapping("/{atividadeId}/pessoas/import")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<List<AtividadePessoaPapel>> importarPessoas(
            @PathVariable Long atividadeId,
            @RequestParam("file") MultipartFile arquivo,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AtividadePessoaPapel> associacoes = atividadePessoaPapelService.importarAssociacoesCsv(atividadeId, arquivo, userDetails.getUsername());
        return ResponseEntity.status(201).body(associacoes);
    }
}

