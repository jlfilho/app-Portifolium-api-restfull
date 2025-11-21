package edu.uea.acadmanage.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.uea.acadmanage.model.TipoCurso;
import edu.uea.acadmanage.model.AuditLog;
import edu.uea.acadmanage.repository.TipoCursoRepository;
import edu.uea.acadmanage.repository.CursoRepository;
import edu.uea.acadmanage.service.exception.ConflitoException;
import edu.uea.acadmanage.service.exception.TipoCursoEmUsoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TipoCursoService {

        private final TipoCursoRepository tipoCursoRepository;
        private final CursoRepository cursoRepository;
        private final AuditLogService auditLogService;
        private final ObjectMapper objectMapper;

        public TipoCursoService(TipoCursoRepository tipoCursoRepository, CursoRepository cursoRepository,
                                AuditLogService auditLogService, ObjectMapper objectMapper) {
                this.tipoCursoRepository = tipoCursoRepository;
                this.cursoRepository = cursoRepository;
                this.auditLogService = auditLogService;
                this.objectMapper = objectMapper;
        }
        
        public List<TipoCurso> listarTodos() {
                return tipoCursoRepository.findAll();
        }

        public Page<TipoCurso> listarPaginadoComFiltro(String nome, Pageable pageable) {
                if (nome != null && !nome.trim().isEmpty()) {
                        return tipoCursoRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
                }
                return tipoCursoRepository.findAll(pageable);
        }

        public TipoCurso recuperarPorId(Long id) {
                return tipoCursoRepository.findById(id)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de curso não encontrado com o ID: " + id));
        }

        public TipoCurso recuperarPorNome(String nome) {
                return tipoCursoRepository.findByNomeIgnoreCase(nome)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de curso não encontrado com o nome: " + nome));
        }

        public TipoCurso salvar(TipoCurso tipoCurso) {
                if (tipoCursoRepository.existsByNomeIgnoreCase(tipoCurso.getNome())) {
                        throw new ConflitoException("Já existe um tipo de curso com o nome: " + tipoCurso.getNome());
                }
                TipoCurso tipoSalvo = tipoCursoRepository.save(tipoCurso);
                
                // CAMADA 2: Audit Log
                auditLogService.log(
                    AuditLog.AuditAction.CREATE,
                    "TipoCurso",
                    tipoSalvo.getId(),
                    null,
                    tipoSalvo,
                    "Tipo de curso criado: " + tipoSalvo.getNome()
                );
                
                return tipoSalvo;
        }

        public TipoCurso atualizar(Long id, TipoCurso novo) {
                TipoCurso existente = this.recuperarPorId(id);

                // Capturar estado antigo para audit log
                TipoCurso oldState = copyTipoCursoForAudit(existente);

                if (!existente.getNome().equalsIgnoreCase(novo.getNome()) && tipoCursoRepository.existsByNomeIgnoreCase(novo.getNome())) {
                        throw new ConflitoException("Já existe um tipo de curso com o nome: " + novo.getNome());
                }

                existente.setNome(novo.getNome());
                TipoCurso tipoAtualizado = tipoCursoRepository.save(existente);
                
                // CAMADA 2: Audit Log
                auditLogService.log(
                    AuditLog.AuditAction.UPDATE,
                    "TipoCurso",
                    tipoAtualizado.getId(),
                    oldState,
                    tipoAtualizado,
                    "Tipo de curso atualizado: " + tipoAtualizado.getNome()
                );
                
                return tipoAtualizado;
        }

        public void deletar(Long id) {
                if (!tipoCursoRepository.existsById(id)) {
                        throw new RecursoNaoEncontradoException("Tipo de curso não encontrado com o ID: " + id);
                }
                
                // Capturar dados para audit log antes de deletar
                TipoCurso tipoCurso = tipoCursoRepository.findById(id).orElse(null);
                String tipoCursoNome = tipoCurso != null ? tipoCurso.getNome() : "ID: " + id;
                
                if (cursoRepository.existsByTipoCurso_Id(id)) {
                        throw new TipoCursoEmUsoException(id);
                }
                tipoCursoRepository.deleteById(id);
                
                // CAMADA 2: Audit Log
                if (tipoCurso != null) {
                    auditLogService.log(
                        AuditLog.AuditAction.DELETE,
                        "TipoCurso",
                        id,
                        tipoCurso,
                        null,
                        "Tipo de curso excluído: " + tipoCursoNome
                    );
                }
        }

        // Método auxiliar para copiar tipo curso para audit log
        private TipoCurso copyTipoCursoForAudit(TipoCurso tipoCurso) {
            try {
                String json = objectMapper.writeValueAsString(tipoCurso);
                return objectMapper.readValue(json, TipoCurso.class);
            } catch (Exception e) {
                // Se falhar a cópia profunda, criar manualmente uma cópia superficial
                TipoCurso copy = new TipoCurso();
                copy.setId(tipoCurso.getId());
                copy.setNome(tipoCurso.getNome());
                return copy;
            }
        }
}


