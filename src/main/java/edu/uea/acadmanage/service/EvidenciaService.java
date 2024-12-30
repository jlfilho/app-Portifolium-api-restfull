package edu.uea.acadmanage.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final Path fileStorageLocation;

    public EvidenciaService(
            EvidenciaRepository evidenciaRepository,
            AtividadeRepository atividadeRepository,
            CursoService cursoService) throws IOException {
        this.evidenciaRepository = evidenciaRepository;
        this.atividadeRepository = atividadeRepository;
        this.cursoService = cursoService;
        this.fileStorageLocation = Paths.get("/evidencias").toAbsolutePath().normalize();
        Files.createDirectories(this.fileStorageLocation);
    }

    // Verificar se atividade está ativa
    private Boolean verificarAtividadeEstaPublicada(Long atividadeId) {
        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Atividade não publicada com o ID: " + atividadeId));
        if (!atividade.IsPublicada()) {
            throw new AcessoNegadoException("Atividade não publicada com o ID: " + atividadeId);
        }
        return atividade.IsPublicada();
    }

    public Boolean isPublicRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
    }

    // Método para buscar uma evidência por ID
    public EvidenciaDTO getEvidenciasPorId(Long evidenciaId) {
        Evidencia evidencia = evidenciaRepository.findById(evidenciaId)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Evidência não encontrada com o ID: " + evidenciaId));

        if (isPublicRequest()) {
            verificarAtividadeEstaPublicada(evidencia.getAtividade().getId());
        }
        return toEvidenciaDTO(evidencia);
    }

    // Método para listar evidências por atividade
    public List<EvidenciaDTO> listarEvidenciasPorAtividade(Long atividadeId) {
        // Verificar se a atividade existe
        if (!atividadeRepository.existsById(atividadeId)) {
            throw new RecursoNaoEncontradoException("Atividade não encontrada com o ID: " + atividadeId);
        }

        if (isPublicRequest()) {
            System.out.println("Public Request");
            verificarAtividadeEstaPublicada(atividadeId);
        }
        System.out.println("Authenticated Request");

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

        // Verificar se o usuário tem permissão para excluir a evidência
        if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para excluir a evidência no curso: " + atividade.getCurso().getId());
        }

        // Excluir a evidência
        evidenciaRepository.deleteById(evidenciaId);
    }

    // Método para salvar uma evidência
    public EvidenciaDTO salvarEvidencia(
            Long atividadeId,
            String legenda,
            MultipartFile file,
            String username)
            throws IOException {
        // Verificar se a atividade associada existe
        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Atividade não encontrada com o ID: " + atividadeId));

        // Verificar se o usuário tem permissão para salvar a evidência
        if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para salvar a evidência no curso: " + atividade.getCurso().getId());
        }

        // salva a foto no disco
        String uniqueFileName = salvarImagem(atividade, file);

        // Criar a entidade Evidência
        Evidencia evidencia = new Evidencia(null, uniqueFileName, legenda, username, atividade);
        // Salvar no banco
        Evidencia evidenciaSalva = evidenciaRepository.save(evidencia);

        // Retornar o DTO da evidência salva
        return toEvidenciaDTO(evidenciaSalva);
    }

    // Método para atualizar uma evidência
    public EvidenciaDTO atualizarEvidencia(Long evidenciaId, String legenda, MultipartFile file, String username)
            throws IOException {

        // Verificar se a evidência existe
        Evidencia evidenciaExistente = evidenciaRepository.findById(evidenciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Evidência não encontrada com o ID: " + evidenciaId));

        // Verificar se o usuário tem permissão para alterar a evidência
        if (!cursoService.verificarAcessoAoCurso(username, evidenciaExistente.getAtividade().getCurso().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para alterar a evidência no curso: "
                            + evidenciaExistente.getAtividade().getCurso().getId());
        }

        // Atualizar os dados da evidência
        evidenciaExistente.setLegenda(legenda);

        if (file != null) {
            if (!excluirImagem(evidenciaExistente.getUrlFoto())) {
                throw new IllegalArgumentException("O arquivo anterior não pode ser removido.");
            }
            evidenciaExistente.setUrlFoto(salvarImagem(evidenciaExistente.getAtividade(), file));
            evidenciaExistente.setCriadoPor(username);
        }

        // Salvar a evidência atualizada
        Evidencia evidenciaAtualizada = evidenciaRepository.save(evidenciaExistente);

        // Retornar o DTO da evidência atualizada
        return toEvidenciaDTO(evidenciaAtualizada);
    }

    // Método para buscar a atividade associada a uma evidência
    public Atividade buscarAtividadePorEvidencia(Long evidenciaId) {
        return evidenciaRepository.findById(evidenciaId)
                .map(Evidencia::getAtividade) // Recupera a atividade associada
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Evidência não encontrada com ID: " + evidenciaId));
    }

    private EvidenciaDTO toEvidenciaDTO(Evidencia evidencia) {
        return new EvidenciaDTO(
                evidencia.getId(),
                evidencia.getAtividade().getId(),
                evidencia.getUrlFoto(),
                evidencia.getLegenda(),
                evidencia.getCriadoPor());
    }

    private Boolean validarImagem(MultipartFile file) {
        // Verificar se o arquivo enviado é uma imagem JPG ou PNG
        Set<String> allowedContentTypes = Set.of("image/jpg", "image/jpeg", "image/png");
        if (!allowedContentTypes.contains(Objects.requireNonNullElse(file.getContentType(), "").toLowerCase())) {
            throw new IllegalArgumentException("O arquivo enviado deve ser um JPG, JPEG ou PNG válido.");
        }

        return true;
    }

    private String salvarImagem(Atividade atividade, MultipartFile file) throws IOException {
        // Verificar se o arquivo enviado é uma imagem JPG ou PNG
        validarImagem(file);

        // Salvar a foto no diretório
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("O arquivo enviado não possui um nome válido.");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String uniqueFileName = atividade.getCurso().getId() + "/" + atividade.getId() + "/"
                + UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
        Files.createDirectories(targetLocation);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    // Método para excluir uma imagem
    private Boolean excluirImagem(String fileName) {
        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(targetLocation);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
