package edu.uea.acadmanage.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.uea.acadmanage.DTO.UnidadeAcademicaDTO;
import edu.uea.acadmanage.model.UnidadeAcademica;
import edu.uea.acadmanage.model.AuditLog;
import edu.uea.acadmanage.repository.CursoRepository;
import edu.uea.acadmanage.repository.UnidadeAcademicaRepository;
import edu.uea.acadmanage.service.exception.ConflitoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UnidadeAcademicaService {

    private final UnidadeAcademicaRepository unidadeAcademicaRepository;
    private final CursoRepository cursoRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public UnidadeAcademicaService(UnidadeAcademicaRepository unidadeAcademicaRepository,
            CursoRepository cursoRepository, AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.unidadeAcademicaRepository = unidadeAcademicaRepository;
        this.cursoRepository = cursoRepository;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    public UnidadeAcademicaDTO salvar(UnidadeAcademicaDTO dto) {
        UnidadeAcademica unidade = new UnidadeAcademica();
        unidade.setNome(dto.nome());
        unidade.setDescricao(dto.descricao());

        UnidadeAcademica salvo = unidadeAcademicaRepository.save(unidade);
        
        // CAMADA 2: Audit Log
        auditLogService.log(
            AuditLog.AuditAction.CREATE,
            "UnidadeAcademica",
            salvo.getId(),
            null,
            salvo,
            "Unidade acadêmica criada: " + salvo.getNome()
        );
        
        return toDTO(salvo);
    }

    public UnidadeAcademicaDTO atualizar(Long id, UnidadeAcademicaDTO dto) {
        UnidadeAcademica unidade = buscarEntidade(id);
        
        // Capturar estado antigo para audit log
        UnidadeAcademica oldState = copyUnidadeAcademicaForAudit(unidade);
        
        unidade.setNome(dto.nome());
        unidade.setDescricao(dto.descricao());
        UnidadeAcademica salvo = unidadeAcademicaRepository.save(unidade);
        
        // CAMADA 2: Audit Log
        auditLogService.log(
            AuditLog.AuditAction.UPDATE,
            "UnidadeAcademica",
            salvo.getId(),
            oldState,
            salvo,
            "Unidade acadêmica atualizada: " + salvo.getNome()
        );
        
        return toDTO(salvo);
    }

    public UnidadeAcademicaDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    public Page<UnidadeAcademicaDTO> listar(String nome, Pageable pageable) {
        Page<UnidadeAcademica> page;
        if (nome != null && !nome.isBlank()) {
            page = unidadeAcademicaRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        } else {
            page = unidadeAcademicaRepository.findAll(pageable);
        }
        return page.map(this::toDTO);
    }

    public void excluir(Long id) {
        UnidadeAcademica unidade = buscarEntidade(id);

        // Capturar dados para audit log antes de deletar
        String unidadeNome = unidade.getNome();
        Long unidadeIdValue = unidade.getId();

        if (cursoRepository.existsByUnidadeAcademica_Id(unidade.getId())) {
            throw new ConflitoException("Não é possível excluir a unidade acadêmica pois existem cursos associados.");
        }

        unidadeAcademicaRepository.delete(unidade);
        
        // CAMADA 2: Audit Log
        auditLogService.log(
            AuditLog.AuditAction.DELETE,
            "UnidadeAcademica",
            unidadeIdValue,
            unidade,
            null,
            "Unidade acadêmica excluída: " + unidadeNome
        );
    }

    public UnidadeAcademica buscarEntidade(Long id) {
        return unidadeAcademicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade acadêmica não encontrada: " + id));
    }

    // Método auxiliar para copiar unidade acadêmica para audit log
    private UnidadeAcademica copyUnidadeAcademicaForAudit(UnidadeAcademica unidade) {
        try {
            String json = objectMapper.writeValueAsString(unidade);
            return objectMapper.readValue(json, UnidadeAcademica.class);
        } catch (Exception e) {
            // Se falhar a cópia profunda, criar manualmente uma cópia superficial
            UnidadeAcademica copy = new UnidadeAcademica();
            copy.setId(unidade.getId());
            copy.setNome(unidade.getNome());
            copy.setDescricao(unidade.getDescricao());
            return copy;
        }
    }

    private UnidadeAcademicaDTO toDTO(UnidadeAcademica unidade) {
        return new UnidadeAcademicaDTO(unidade.getId(), unidade.getNome(), unidade.getDescricao());
    }
}

