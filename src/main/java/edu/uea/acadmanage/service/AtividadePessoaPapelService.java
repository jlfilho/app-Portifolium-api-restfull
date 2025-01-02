package edu.uea.acadmanage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.AtividadePessoaId;
import edu.uea.acadmanage.model.AtividadePessoaPapel;
import edu.uea.acadmanage.model.Papel;
import edu.uea.acadmanage.model.Pessoa;
import edu.uea.acadmanage.repository.AtividadePessoaPapelRepository;
import edu.uea.acadmanage.repository.AtividadeRepository;
import edu.uea.acadmanage.repository.PessoaRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import edu.uea.acadmanage.service.exception.ConflitoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class AtividadePessoaPapelService {

    private final AtividadePessoaPapelRepository papelRepository;
    private final AtividadeRepository atividadeRepository;
    private final PessoaRepository pessoaRepository;
    private final CursoService cursoService;

    public AtividadePessoaPapelService(AtividadePessoaPapelRepository papelRepository,
                                       AtividadeRepository atividadeRepository,
                                       PessoaRepository pessoaRepository,
                                       CursoService cursoService) {
        this.papelRepository = papelRepository;
        this.atividadeRepository = atividadeRepository;
        this.pessoaRepository = pessoaRepository;
        this.cursoService = cursoService;
    }

    public AtividadePessoaPapel associarPessoa(Long atividadeId, Long pessoaId, Papel papel, String username) {
        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada"));

        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));
        
        // Verificar se o usuário tem permissão para salvar a atividade
        if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão incluir integrante em atividades do curso: " + atividade.getCurso().getId());
        }

        boolean jaAssociada = papelRepository.existsByAtividadeAndPessoa(atividade, pessoa);
        if (jaAssociada) {
            throw new ConflitoException("Pessoa já está associada a esta atividade.");
        }
        // Cria o ID composto
        AtividadePessoaId id = new AtividadePessoaId(atividadeId, pessoaId);
        AtividadePessoaPapel associacao = new AtividadePessoaPapel();
        associacao.setId(id);
        associacao.setAtividade(atividade);
        associacao.setPessoa(pessoa);
        associacao.setPapel(papel);

        return papelRepository.save(associacao);
    }

    public AtividadePessoaPapel alterarPapel(AtividadePessoaId id, Papel novoPapel, String username) {
        AtividadePessoaPapel associacao = papelRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Associação não encontrada"));

        Atividade atividade = atividadeRepository.findById(associacao.getAtividade().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada"));
         // Verificar se o usuário tem permissão para salvar a atividade
         if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão incluir integrante em atividades do curso: " + atividade.getCurso().getId());
        }

        associacao.setPapel(novoPapel);
        return papelRepository.save(associacao);
    }

    public List<AtividadePessoaPapel> listarPorAtividade(Long atividadeId) {
        return papelRepository.findByAtividadeId(atividadeId);
    }


    public void removerPessoaDaAtividade(Long atividadeId, Long pessoaId, String username) {
        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada"));
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));
        
        // Verificar se o usuário tem permissão para salvar a atividade
        if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão incluir integrante em atividades do curso: " + atividade.getCurso().getId());
        }

        boolean exists = papelRepository.existsByAtividadeAndPessoa(atividade, pessoa);
        if (!exists) {
            throw new RecursoNaoEncontradoException("A pessoa não está associada à atividade.");
        }

        papelRepository.deleteById(new AtividadePessoaId(atividadeId, pessoaId));
    }
}
