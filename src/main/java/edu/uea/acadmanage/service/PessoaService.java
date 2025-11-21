package edu.uea.acadmanage.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.uea.acadmanage.DTO.PessoaDTO;
import edu.uea.acadmanage.DTO.PessoaImportResponseDTO;
import edu.uea.acadmanage.model.Pessoa;
import edu.uea.acadmanage.repository.PessoaRepository;
import edu.uea.acadmanage.service.exception.ConflitoException;
import edu.uea.acadmanage.service.exception.ErroProcessamentoArquivoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;
import edu.uea.acadmanage.service.exception.ValidacaoException;
import edu.uea.acadmanage.model.AuditLog;
import edu.uea.acadmanage.model.ActionLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final AuditLogService auditLogService;
    private final ActionLogService actionLogService;
    private final ObjectMapper objectMapper;

    public PessoaService(PessoaRepository pessoaRepository, AuditLogService auditLogService, 
                         ActionLogService actionLogService, ObjectMapper objectMapper) {
        this.pessoaRepository = pessoaRepository;
        this.auditLogService = auditLogService;
        this.actionLogService = actionLogService;
        this.objectMapper = objectMapper;
    }

    public Page<PessoaDTO> listar(String nome, Pageable pageable) {
        Page<Pessoa> page;
        if (nome != null && !nome.trim().isEmpty()) {
            page = pessoaRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        } else {
            page = pessoaRepository.findAll(pageable);
        }
        return page.map(this::toDTO);
    }

    @Cacheable(value = "pessoas", key = "#id")
    public PessoaDTO buscarPorId(Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada: " + id));
        return toDTO(pessoa);
    }

    @CacheEvict(value = "pessoas", allEntries = true)
    public PessoaDTO criar(PessoaDTO dto) {
        String cpfNormalizado = normalizarCpf(dto.cpf());
        if (cpfNormalizado.isEmpty()) {
            throw new ValidacaoException("CPF inválido.");
        }
        verificarCpfDuplicado(cpfNormalizado);

        Pessoa pessoa = new Pessoa();
        pessoa.setNome(dto.nome().trim());
        pessoa.setCpf(cpfNormalizado);

        Pessoa salva = pessoaRepository.save(pessoa);
        
        // CAMADA 2: Audit Log
        auditLogService.log(
            AuditLog.AuditAction.CREATE,
            "Pessoa",
            salva.getId(),
            null,
            salva,
            "Pessoa criada: " + salva.getNome()
        );
        
        return toDTO(salva);
    }

    @CacheEvict(value = "pessoas", key = "#id", allEntries = true)
    public PessoaDTO atualizar(Long id, PessoaDTO dto) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada: " + id));

        // Capturar estado antigo para audit log
        Pessoa oldState = copyPessoaForAudit(pessoa);

        String cpfNormalizado = normalizarCpf(dto.cpf());
        if (cpfNormalizado.isEmpty()) {
            throw new ValidacaoException("CPF inválido.");
        }
        
        // Obter CPF atual normalizado diretamente do campo (sem formatação)
        String cpfAtualNoBanco = pessoa.getCpfNormalizado();
        
        // Só verificar duplicação se o CPF realmente mudou
        if (!cpfNormalizado.equals(cpfAtualNoBanco)) {
            // Verificar se o novo CPF já existe em outra pessoa (excluindo a pessoa atual)
            if (pessoaRepository.existsByCpfAndIdNot(cpfNormalizado, id)) {
                Pessoa existente = pessoaRepository.findByCpf(cpfNormalizado).orElse(null);
                String nomeDuplicado = existente != null ? existente.getNome() : "";
                throw new ConflitoException("CPF já cadastrado para a pessoa: " + nomeDuplicado);
            }
            // Atualizar o CPF apenas se mudou
            pessoa.setCpf(cpfNormalizado);
        }
        // Se o CPF não mudou, não atualizamos (evita validação desnecessária)

        pessoa.setNome(dto.nome().trim());

        Pessoa salva = pessoaRepository.save(pessoa);
        
        // CAMADA 2: Audit Log
        auditLogService.log(
            AuditLog.AuditAction.UPDATE,
            "Pessoa",
            salva.getId(),
            oldState,
            salva,
            "Pessoa atualizada: " + salva.getNome()
        );
        
        return toDTO(salva);
    }

    @CacheEvict(value = "pessoas", key = "#id", allEntries = true)
    public void excluir(Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada: " + id));
        
        // Capturar dados para audit log antes de deletar
        String pessoaNome = pessoa.getNome();
        Long pessoaIdValue = pessoa.getId();
        
        pessoaRepository.delete(pessoa);
        
        // CAMADA 2: Audit Log
        auditLogService.log(
            AuditLog.AuditAction.DELETE,
            "Pessoa",
            pessoaIdValue,
            pessoa,
            null,
            "Pessoa excluída: " + pessoaNome
        );
    }

    @CacheEvict(value = "pessoas", allEntries = true)
    public PessoaImportResponseDTO importarCsv(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ValidacaoException("Arquivo CSV não informado.");
        }

        List<String> cadastrados = new ArrayList<>();
        List<String> duplicados = new ArrayList<>();
        Set<String> cpfsProcessados = new HashSet<>();
        int totalProcessados = 0;

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
                    String textoLower = texto.toLowerCase();
                    if (textoLower.contains("nome") && textoLower.contains("cpf")) {
                        cabecalhoVerificado = true;
                        continue;
                    }
                    cabecalhoVerificado = true;
                }

                totalProcessados++;
                String[] partes = dividirLinhaCsv(texto);
                if (partes.length < 2) {
                    duplicados.add("Linha inválida: " + texto);
                    continue;
                }

                String nome = partes[0].trim();
                String cpfNormalizado = normalizarCpf(partes[1]);
                if (nome.isEmpty() || cpfNormalizado.isEmpty()) {
                    duplicados.add("Dados incompletos: " + texto);
                    continue;
                }

                if (cpfsProcessados.contains(cpfNormalizado)) {
                    duplicados.add(nome + " (CPF duplicado no arquivo)");
                    continue;
                }

                cpfsProcessados.add(cpfNormalizado);

                Pessoa existente = pessoaRepository.findByCpf(cpfNormalizado).orElse(null);
                if (existente != null) {
                    duplicados.add(nome + " - já cadastrada como " + existente.getNome());
                    continue;
                }

                Pessoa nova = new Pessoa();
                nova.setNome(nome);
                nova.setCpf(cpfNormalizado);
                Pessoa salva = pessoaRepository.save(nova);
                
                // CAMADA 2: Audit Log - registrar cada pessoa criada
                auditLogService.log(
                    AuditLog.AuditAction.CREATE,
                    "Pessoa",
                    salva.getId(),
                    null,
                    salva,
                    "Pessoa criada via importação CSV: " + salva.getNome()
                );
                
                cadastrados.add(nome);
            }
        } catch (IOException e) {
            // CAMADA 3: Action Log - importação falhou
            actionLogService.log(
                ActionLog.ActionType.IMPORT_CSV,
                false,
                "Erro ao importar pessoas via CSV",
                e.getMessage(),
                new HashMap<>()
            );
            throw new ErroProcessamentoArquivoException("Não foi possível ler o arquivo CSV.", e);
        }

        // CAMADA 3: Action Log - importação concluída com sucesso
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("totalProcessados", totalProcessados);
        metadata.put("totalCadastrados", cadastrados.size());
        metadata.put("totalDuplicados", duplicados.size());
        metadata.put("nomeArquivo", arquivo.getOriginalFilename());
        metadata.put("tamanhoArquivo", arquivo.getSize());
        
        actionLogService.log(
            ActionLog.ActionType.IMPORT_CSV,
            true,
            "Importação de pessoas via CSV concluída: " + cadastrados.size() + " cadastradas, " + 
            duplicados.size() + " duplicadas/ignoradas",
            null,
            metadata
        );

        return new PessoaImportResponseDTO(totalProcessados, cadastrados.size(), cadastrados, duplicados);
    }

    private String[] dividirLinhaCsv(String texto) {
        String[] partes = texto.split(";");
        if (partes.length <= 1) {
            partes = texto.split(",");
        }
        return partes;
    }

    private void verificarCpfDuplicado(String cpfNormalizado) {
        if (pessoaRepository.existsByCpf(cpfNormalizado)) {
            Pessoa pessoa = pessoaRepository.findByCpf(cpfNormalizado).orElse(null);
            String nome = pessoa != null ? pessoa.getNome() : "";
            throw new ConflitoException("CPF já cadastrado para a pessoa: " + nome);
        }
    }

    private String normalizarCpf(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("\\D", "");
    }

    // Método auxiliar para copiar pessoa para audit log
    private Pessoa copyPessoaForAudit(Pessoa pessoa) {
        try {
            String json = objectMapper.writeValueAsString(pessoa);
            return objectMapper.readValue(json, Pessoa.class);
        } catch (Exception e) {
            // Se falhar a cópia profunda, criar manualmente uma cópia superficial
            Pessoa copy = new Pessoa();
            copy.setId(pessoa.getId());
            copy.setNome(pessoa.getNome());
            copy.setCpf(pessoa.getCpfNormalizado());
            return copy;
        }
    }

    private PessoaDTO toDTO(Pessoa pessoa) {
        boolean possuiUsuario = pessoa.getUsuario() != null;
        return new PessoaDTO(pessoa.getId(), pessoa.getNome(), pessoa.getCpf(), possuiUsuario);
    }
}

