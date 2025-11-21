package edu.uea.acadmanage.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import edu.uea.acadmanage.DTO.EvidenciaDTO;
import edu.uea.acadmanage.DTO.EvidenciaOrdemDTO;
import edu.uea.acadmanage.config.FileStorageProperties;
import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.Evidencia;
import edu.uea.acadmanage.repository.AtividadeRepository;
import edu.uea.acadmanage.repository.EvidenciaRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import edu.uea.acadmanage.service.exception.ArquivoInvalidoException;
import edu.uea.acadmanage.service.exception.ErroProcessamentoArquivoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;
import edu.uea.acadmanage.service.exception.ValidacaoException;
import edu.uea.acadmanage.model.AuditLog;
import edu.uea.acadmanage.model.ActionLog;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EvidenciaService {

    private final EvidenciaRepository evidenciaRepository;
    private final AtividadeRepository atividadeRepository;
    private final CursoService cursoService;
    private final AtividadeAutorizacaoService atividadeAutorizacaoService;
    private final AuditLogService auditLogService;
    private final ActionLogService actionLogService;
    private final ObjectMapper objectMapper;
    private final Path fileStorageLocation;
    private final String baseStorageLocation;

    public EvidenciaService(
            EvidenciaRepository evidenciaRepository,
            AtividadeRepository atividadeRepository,
            CursoService cursoService,
            AtividadeAutorizacaoService atividadeAutorizacaoService,
            AuditLogService auditLogService,
            ActionLogService actionLogService,
            ObjectMapper objectMapper,
            FileStorageProperties fileStorageProperties) throws IOException {
        this.evidenciaRepository = evidenciaRepository;
        this.atividadeRepository = atividadeRepository;
        this.cursoService = cursoService;
        this.atividadeAutorizacaoService = atividadeAutorizacaoService;
        this.auditLogService = auditLogService;
        this.actionLogService = actionLogService;
        this.objectMapper = objectMapper;
        this.baseStorageLocation = "/evidencias";
        this.fileStorageLocation = Paths.get(fileStorageProperties.getStorageLocation()+this.baseStorageLocation).toAbsolutePath().normalize();
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
        return evidenciaRepository.findByAtividadeIdOrderByOrdemAsc(atividadeId).stream()
                .map(this::toEvidenciaDTO)
                .collect(Collectors.toList());
    }

    // Método para excluir uma evidência
    public void excluirEvidencia(Long evidenciaId, String username) {
        // Verificar se a evidência existe
        Evidencia evidenciaExistente = evidenciaRepository.findById(evidenciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Evidência não encontrada com o ID: " + evidenciaId));

        // Capturar dados para audit log antes de deletar
        String evidenciaLegenda = evidenciaExistente.getLegenda();
        Long evidenciaIdValue = evidenciaExistente.getId();
        String evidenciaUrlFoto = evidenciaExistente.getUrlFoto();

        // Verificar se a atividade associada existe e obter o ID da atividade
        Long atividadeId = evidenciaExistente.getAtividade().getId();
        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Atividade não encontrada com o ID: " + atividadeId));

        // Verificar se o usuário tem permissão para gerenciar evidências desta atividade
        if (!atividadeAutorizacaoService.podeGerenciarEvidencias(username, atividadeId)) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para gerenciar evidências desta atividade: " + atividadeId);
        }

        // Excluir a evidência
        evidenciaRepository.deleteById(evidenciaId);
        excluirImagem(evidenciaExistente.getUrlFoto());
        compactarOrdem(atividade.getId());

        // CAMADA 2: Audit Log
        auditLogService.log(
            AuditLog.AuditAction.DELETE,
            "Evidencia",
            evidenciaIdValue,
            evidenciaExistente,
            null,
            "Evidência excluída: " + evidenciaLegenda
        );

        // CAMADA 3: Action Log - Exclusão de arquivo
        actionLogService.log(
            ActionLog.ActionType.FILE_DELETE,
            true,
            "Arquivo de evidência excluído: " + evidenciaUrlFoto,
            null,
            null
        );
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

        // Verificar se o usuário tem permissão para gerenciar evidências desta atividade
        if (!atividadeAutorizacaoService.podeGerenciarEvidencias(username, atividadeId)) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para gerenciar evidências desta atividade: " + atividadeId);
        }

        // salva a foto no disco
        String uniqueFileName = salvarImagem(atividade, file);

        // Criar a entidade Evidência
        Evidencia evidencia = new Evidencia();
        evidencia.setUrlFoto(uniqueFileName);
        evidencia.setLegenda(legenda);
        evidencia.setCriadoPor(username);
        evidencia.setAtividade(atividade);
        evidencia.setOrdem(proximaOrdem(atividadeId));
        // Salvar no banco
        Evidencia evidenciaSalva = evidenciaRepository.save(evidencia);

        // CAMADA 2: Audit Log
        auditLogService.log(
            AuditLog.AuditAction.CREATE,
            "Evidencia",
            evidenciaSalva.getId(),
            null,
            evidenciaSalva,
            "Evidência criada: " + evidenciaSalva.getLegenda()
        );

        // CAMADA 3: Action Log - Upload de arquivo
        actionLogService.log(
            ActionLog.ActionType.FILE_UPLOAD,
            true,
            "Arquivo de evidência enviado: " + uniqueFileName + " para atividade ID: " + atividadeId,
            null,
            null
        );

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

        // Capturar estado antigo para audit log
        Evidencia oldState = copyEvidenciaForAudit(evidenciaExistente);
        String oldUrlFoto = evidenciaExistente.getUrlFoto();

        // Buscar a atividade da evidência e verificar permissão
        Long atividadeId = evidenciaExistente.getAtividade().getId();
        if (!atividadeAutorizacaoService.podeGerenciarEvidencias(username, atividadeId)) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para gerenciar evidências desta atividade: " + atividadeId);
        }

        // Atualizar os dados da evidência
        evidenciaExistente.setLegenda(legenda);

        if (file != null) {
            if (!excluirImagem(evidenciaExistente.getUrlFoto())) {
                throw new ErroProcessamentoArquivoException("O arquivo anterior não pode ser removido.");
            }
            evidenciaExistente.setUrlFoto(salvarImagem(evidenciaExistente.getAtividade(), file));
            evidenciaExistente.setCriadoPor(username);
        }

        // Salvar a evidência atualizada
        Evidencia evidenciaAtualizada = evidenciaRepository.save(evidenciaExistente);

        // CAMADA 2: Audit Log
        auditLogService.log(
            AuditLog.AuditAction.UPDATE,
            "Evidencia",
            evidenciaAtualizada.getId(),
            oldState,
            evidenciaAtualizada,
            "Evidência atualizada: " + evidenciaAtualizada.getLegenda()
        );

        // CAMADA 3: Action Log - Upload de arquivo (se arquivo foi substituído)
        if (file != null && !evidenciaAtualizada.getUrlFoto().equals(oldUrlFoto)) {
            actionLogService.log(
                ActionLog.ActionType.FILE_UPLOAD,
                true,
                "Arquivo de evidência atualizado: " + evidenciaAtualizada.getUrlFoto(),
                null,
                null
            );
        }

        // Retornar o DTO da evidência atualizada
        return toEvidenciaDTO(evidenciaAtualizada);
    }

    @Transactional
    public List<EvidenciaDTO> atualizarOrdemEvidencias(Long atividadeId, List<EvidenciaOrdemDTO> ordens, String username) {
        if (ordens == null || ordens.isEmpty()) {
            throw new ValidacaoException("A lista de ordens não pode ser vazia.");
        }

        // Verificar se o usuário tem permissão para gerenciar evidências desta atividade
        // (o método podeGerenciarEvidencias já verifica se a atividade existe)
        if (!atividadeAutorizacaoService.podeGerenciarEvidencias(username, atividadeId)) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para gerenciar evidências desta atividade: " + atividadeId);
        }

        List<Evidencia> evidencias = evidenciaRepository.findByAtividadeIdOrderByOrdemAsc(atividadeId);
        if (evidencias.isEmpty()) {
            throw new RecursoNaoEncontradoException("Não há evidências cadastradas para esta atividade.");
        }

        Map<Long, Evidencia> evidenciasPorId = evidencias.stream()
                .collect(Collectors.toMap(Evidencia::getId, evidencia -> evidencia));

        if (ordens.size() != evidenciasPorId.size()) {
            throw new ValidacaoException("A lista de ordens deve conter todas as evidências da atividade.");
        }

        Set<Long> idsProcessados = new HashSet<>();
        Set<Integer> ordensProcessadas = new HashSet<>();

        ordens.forEach(ordemDTO -> {
            if (!idsProcessados.add(ordemDTO.evidenciaId())) {
                throw new ValidacaoException("ID de evidência duplicado na requisição: " + ordemDTO.evidenciaId());
            }
            if (!ordensProcessadas.add(ordemDTO.ordem())) {
                throw new ValidacaoException("Valor de ordem duplicado na requisição: " + ordemDTO.ordem());
            }

            Evidencia evidencia = evidenciasPorId.get(ordemDTO.evidenciaId());
            if (evidencia == null) {
                throw new RecursoNaoEncontradoException("Evidência não encontrada na atividade: " + ordemDTO.evidenciaId());
            }
            evidencia.setOrdem(ordemDTO.ordem());
        });

        evidenciaRepository.saveAll(evidencias);

        return evidenciaRepository.findByAtividadeIdOrderByOrdemAsc(atividadeId).stream()
                .map(this::toEvidenciaDTO)
                .collect(Collectors.toList());
    }

    // Método para buscar a atividade associada a uma evidência
    public Atividade buscarAtividadePorEvidencia(Long evidenciaId) {
        return evidenciaRepository.findById(evidenciaId)
                .map(Evidencia::getAtividade) // Recupera a atividade associada
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Evidência não encontrada com ID: " + evidenciaId));
    }

    // Método auxiliar para copiar evidência para audit log
    private Evidencia copyEvidenciaForAudit(Evidencia evidencia) {
        try {
            // Usar ObjectMapper para criar uma cópia profunda do objeto
            String json = objectMapper.writeValueAsString(evidencia);
            return objectMapper.readValue(json, Evidencia.class);
        } catch (Exception e) {
            // Se falhar a cópia profunda, criar manualmente uma cópia superficial
            Evidencia copy = new Evidencia();
            copy.setId(evidencia.getId());
            copy.setUrlFoto(evidencia.getUrlFoto());
            copy.setLegenda(evidencia.getLegenda());
            copy.setOrdem(evidencia.getOrdem());
            copy.setCriadoPor(evidencia.getCriadoPor());
            copy.setAtividade(evidencia.getAtividade());
            return copy;
        }
    }

    private EvidenciaDTO toEvidenciaDTO(Evidencia evidencia) {
        return new EvidenciaDTO(
                evidencia.getId(),
                evidencia.getAtividade().getId(),
                evidencia.getUrlFoto(),
                evidencia.getLegenda(),
                evidencia.getOrdem(),
                evidencia.getCriadoPor());
    }

    private int proximaOrdem(Long atividadeId) {
        Integer maxOrdem = evidenciaRepository.findMaxOrdemByAtividadeId(atividadeId);
        if (maxOrdem == null) {
            maxOrdem = -1;
        }
        return maxOrdem + 1;
    }

    private void compactarOrdem(Long atividadeId) {
        List<Evidencia> evidenciasOrdenadas = evidenciaRepository.findByAtividadeIdOrderByOrdemAsc(atividadeId);
        int ordem = 0;
        for (Evidencia evidencia : evidenciasOrdenadas) {
            if (evidencia.getOrdem() == null || evidencia.getOrdem() != ordem) {
                evidencia.setOrdem(ordem);
            }
            ordem++;
        }
        evidenciaRepository.saveAll(evidenciasOrdenadas);
    }

    private Boolean validarImagem(MultipartFile file) {
        // Verificar se o arquivo enviado é uma imagem JPG ou PNG
        Set<String> allowedContentTypes = Set.of("image/jpg", "image/jpeg", "image/png");
        if (!allowedContentTypes.contains(Objects.requireNonNullElse(file.getContentType(), "").toLowerCase())) {
            throw new ArquivoInvalidoException("O arquivo enviado deve ser um JPG, JPEG ou PNG válido.");
        }

        return true;
    }

    private String salvarImagem(Atividade atividade, MultipartFile file) throws IOException {
        // Verificar se o arquivo enviado é uma imagem JPG ou PNG
        validarImagem(file);

        // Salvar a foto no diretório
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new ArquivoInvalidoException("O arquivo enviado não possui um nome válido.");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String uniqueFileName = atividade.getCurso().getId() + "/" + atividade.getId() + "/"
                + UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName).normalize();
        Files.createDirectories(targetLocation.getParent());
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return this.baseStorageLocation + "/" + uniqueFileName;
    }

    // Método para excluir uma imagem
    private Boolean excluirImagem(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }

        try {
            String normalized = fileName.replace("\\", "/");

            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }

            String basePath = this.baseStorageLocation.replace("\\", "/");
            if (basePath.startsWith("/")) {
                basePath = basePath.substring(1);
            }

            if (normalized.startsWith(basePath)) {
                normalized = normalized.substring(basePath.length());
                if (normalized.startsWith("/")) {
                    normalized = normalized.substring(1);
                }
            }

            Path targetLocation = this.fileStorageLocation.resolve(normalized).normalize();
            Files.deleteIfExists(targetLocation);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
