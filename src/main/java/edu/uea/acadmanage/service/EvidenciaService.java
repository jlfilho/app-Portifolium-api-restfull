package edu.uea.acadmanage.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import edu.uea.acadmanage.DTO.EvidenciaDTO;
import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.Evidencia;
import edu.uea.acadmanage.repository.AtividadeRepository;
import edu.uea.acadmanage.repository.EvidenciaRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class EvidenciaService {

    private final EvidenciaRepository evidenciaRepository;
    private final AtividadeRepository atividadeRepository;
    private final CursoService cursoService;

    public EvidenciaService(
        EvidenciaRepository evidenciaRepository, 
        AtividadeRepository atividadeRepository,
        CursoService cursoService) {
        this.evidenciaRepository = evidenciaRepository;
        this.atividadeRepository = atividadeRepository;
        this.cursoService = cursoService;
    }

    // Método para salvar uma evidência
    public EvidenciaDTO salvarEvidencia(EvidenciaDTO evidenciaDTO, String username) {
        // Verificar se a atividade associada existe
        Atividade atividade = atividadeRepository.findById(evidenciaDTO.atividadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Atividade não encontrada com o ID: " + evidenciaDTO.atividadeId()));

        //Verificar se o usuário tem permissão para salvar a evidência
        if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException("Usuário não tem permissão para salvar a evidência no curso: " + atividade.getCurso().getId());
        }

        // Criar a entidade Evidência
        Evidencia evidencia = toEvidencia(evidenciaDTO, atividade);

        // Salvar no banco
        Evidencia evidenciaSalva = evidenciaRepository.save(evidencia);

        // Retornar o DTO da evidência salva
        return toEvidenciaDTO(evidenciaSalva);
    }

    // Método para atualizar uma evidência
    public EvidenciaDTO atualizarEvidencia(Long evidenciaId, EvidenciaDTO evidenciaDTO, String username) {
        // Verificar se a evidência existe
        Evidencia evidenciaExistente = evidenciaRepository.findById(evidenciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Evidência não encontrada com o ID: " + evidenciaId));

        // Verificar se a atividade associada existe
        Atividade atividade = atividadeRepository.findById(evidenciaDTO.atividadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Atividade não encontrada com o ID: " + evidenciaDTO.atividadeId()));

        //Verificar se o usuário tem permissão para alterar a evidência
        if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException("Usuário não tem permissão para alterar a evidência no curso: " + atividade.getCurso().getId());
        }

        //Verificar se o usuário tem permissão para alterar a evidência
        if (!cursoService.verificarAcessoAoCurso(username, evidenciaExistente.getAtividade().getCurso().getId())) {
            throw new AcessoNegadoException("Usuário não tem permissão para alterar a evidência no curso: " + atividade.getCurso().getId());
        }

        // Atualizar os dados da evidência
        evidenciaExistente.setFoto(evidenciaDTO.foto());
        evidenciaExistente.setLegenda(evidenciaDTO.legenda());
        evidenciaExistente.setAtividade(atividade);

        // Salvar a evidência atualizada
        Evidencia evidenciaAtualizada = evidenciaRepository.save(evidenciaExistente);

        // Retornar o DTO da evidência atualizada
        return toEvidenciaDTO(evidenciaAtualizada);
    }

    // Método para listar evidências por atividade
    public List<EvidenciaDTO> listarEvidenciasPorAtividade(Long atividadeId) {
        // Verificar se a atividade existe
        if (!atividadeRepository.existsById(atividadeId)) {
            throw new RecursoNaoEncontradoException("Atividade não encontrada com o ID: " + atividadeId);
    }

        // Buscar evidências relacionadas à atividade
        return evidenciaRepository.findByAtividadeId(atividadeId).stream()
                .map(this::toEvidenciaDTO)
                .collect(Collectors.toList());
    }

    // Método para excluir uma evidência
    public void excluirEvidencia(Long evidenciaId, String username) {
        // Verificar se a evidência existe
        Evidencia evidenciaExistente = evidenciaRepository.findById(evidenciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Evidência não encontrada com o ID: " + evidenciaId));
        
        // Verificar se a atividade associada existe
        Atividade atividade = atividadeRepository.findById(evidenciaExistente.getAtividade().getId()) 
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Atividade não encontrada com o ID: " + evidenciaExistente.getAtividade().getId()));

        //Verificar se o usuário tem permissão para excluir a evidência
        if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException("Usuário não tem permissão para excluir a evidência no curso: " + atividade.getCurso().getId());
        }

        // Excluir a evidência
        evidenciaRepository.deleteById(evidenciaId);
    }

    // Método para buscar a atividade associada a uma evidência
    public Atividade buscarAtividadePorEvidencia(Long evidenciaId) {
        return evidenciaRepository.findById(evidenciaId)
                .map(Evidencia::getAtividade) // Recupera a atividade associada
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evidência não encontrada com ID: " + evidenciaId));
    }

    private EvidenciaDTO toEvidenciaDTO(Evidencia evidencia) {
        return new EvidenciaDTO(
                evidencia.getId(),
                evidencia.getAtividade().getId(),
                evidencia.getFoto(),
                evidencia.getLegenda());
    }

    private Evidencia toEvidencia(EvidenciaDTO evidenciaDTO, Atividade atividade) {
        Evidencia evidencia = new Evidencia();
        evidencia.setFoto(evidenciaDTO.foto());
        evidencia.setLegenda(evidenciaDTO.legenda());
        evidencia.setAtividade(atividade);
        return evidencia;
    }

}
