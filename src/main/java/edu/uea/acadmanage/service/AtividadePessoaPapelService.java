package edu.uea.acadmanage.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.uea.acadmanage.DTO.PessoaPapelDTO;
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
import edu.uea.acadmanage.service.exception.ErroProcessamentoArquivoException;
import edu.uea.acadmanage.service.exception.ValidacaoException;
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

    public List<PessoaPapelDTO> listarPorAtividade(Long atividadeId) {
        return papelRepository.findByAtividadeId(atividadeId).stream()
                .map(associacao -> new PessoaPapelDTO(
                        associacao.getPessoa().getId(),
                        associacao.getPessoa().getNome(),
                        associacao.getPessoa().getCpf(),
                        associacao.getPapel().name()))
                .toList();
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

    public List<AtividadePessoaPapel> importarAssociacoesCsv(Long atividadeId, MultipartFile arquivo, String username) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ValidacaoException("Arquivo CSV não informado.");
        }

        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada"));

        if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão incluir integrante em atividades do curso: " + atividade.getCurso().getId());
        }

        List<AtividadePessoaPapel> associacoesCriadas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {
            String linha;
            boolean cabecalhoVerificado = false;

            while ((linha = reader.readLine()) != null) {
                String texto = linha.trim();
                if (texto.isEmpty()) {
                    continue;
                }

                if (!cabecalhoVerificado) {
                    String lower = texto.toLowerCase(Locale.ROOT);
                    if (lower.contains("nome") && lower.contains("cpf") && lower.contains("papel")) {
                        cabecalhoVerificado = true;
                        continue;
                    }
                    cabecalhoVerificado = true;
                }

                String[] partes = dividirLinhaCsv(texto);
                if (partes.length < 3) {
                    throw new ValidacaoException("Linha inválida no CSV: " + texto);
                }

                String nome = partes[0].trim();
                String cpf = normalizarCpf(partes[1]);
                String papelTexto = partes[2].trim();

                if (nome.isEmpty() || cpf.isEmpty() || papelTexto.isEmpty()) {
                    throw new ValidacaoException("Dados incompletos no CSV: " + texto);
                }

                Papel papel = parsePapel(papelTexto);

                Pessoa pessoa = pessoaRepository.findByCpf(cpf)
                        .orElseGet(() -> pessoaRepository.save(new Pessoa(null, nome, cpf)));

                if (papelRepository.existsByAtividadeAndPessoa(atividade, pessoa)) {
                    throw new ConflitoException("Pessoa " + nome + " já está associada a esta atividade.");
                }

                AtividadePessoaPapel associacao = new AtividadePessoaPapel();
                associacao.setId(new AtividadePessoaId(atividadeId, pessoa.getId()));
                associacao.setAtividade(atividade);
                associacao.setPessoa(pessoa);
                associacao.setPapel(papel);

                associacoesCriadas.add(papelRepository.save(associacao));
            }
        } catch (IOException e) {
            throw new ErroProcessamentoArquivoException("Não foi possível ler o arquivo CSV.", e);
        }

        return associacoesCriadas;
    }

    private String[] dividirLinhaCsv(String texto) {
        String[] partes = texto.split(";");
        if (partes.length <= 1) {
            partes = texto.split(",");
        }
        return partes;
    }

    private Papel parsePapel(String texto) {
        try {
            return Papel.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            String valores = List.of(Papel.values()).stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new ValidacaoException("Papel inválido: " + texto + ". Valores permitidos: " + valores);
        }
    }

    private String normalizarCpf(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("\\D", "");
    }
}
