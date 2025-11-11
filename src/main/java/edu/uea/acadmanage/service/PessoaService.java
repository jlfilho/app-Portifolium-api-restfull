package edu.uea.acadmanage.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.uea.acadmanage.DTO.PessoaDTO;
import edu.uea.acadmanage.DTO.PessoaImportResponseDTO;
import edu.uea.acadmanage.model.Pessoa;
import edu.uea.acadmanage.repository.PessoaRepository;
import edu.uea.acadmanage.service.exception.ConflitoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;

    public PessoaService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
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

    public PessoaDTO buscarPorId(Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada: " + id));
        return toDTO(pessoa);
    }

    public PessoaDTO criar(PessoaDTO dto) {
        String cpfNormalizado = normalizarCpf(dto.cpf());
        if (cpfNormalizado.isEmpty()) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        verificarCpfDuplicado(cpfNormalizado);

        Pessoa pessoa = new Pessoa();
        pessoa.setNome(dto.nome().trim());
        pessoa.setCpf(cpfNormalizado);

        Pessoa salva = pessoaRepository.save(pessoa);
        return toDTO(salva);
    }

    public PessoaDTO atualizar(Long id, PessoaDTO dto) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada: " + id));

        String cpfNormalizado = normalizarCpf(dto.cpf());
        if (cpfNormalizado.isEmpty()) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        if (!pessoa.getCpf().equals(cpfNormalizado) && pessoaRepository.existsByCpf(cpfNormalizado)) {
            Pessoa existente = pessoaRepository.findByCpf(cpfNormalizado).orElse(null);
            String nomeDuplicado = existente != null ? existente.getNome() : "";
            throw new ConflitoException("CPF já cadastrado para a pessoa: " + nomeDuplicado);
        }

        pessoa.setNome(dto.nome().trim());
        pessoa.setCpf(cpfNormalizado);

        Pessoa salva = pessoaRepository.save(pessoa);
        return toDTO(salva);
    }

    public void excluir(Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada: " + id));
        pessoaRepository.delete(pessoa);
    }

    public PessoaImportResponseDTO importarCsv(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo CSV não informado.");
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
                pessoaRepository.save(nova);
                cadastrados.add(nome);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo CSV.", e);
        }

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

    private PessoaDTO toDTO(Pessoa pessoa) {
        boolean possuiUsuario = pessoa.getUsuario() != null;
        return new PessoaDTO(pessoa.getId(), pessoa.getNome(), pessoa.getCpf(), possuiUsuario);
    }
}

