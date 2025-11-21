package edu.uea.acadmanage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.uea.acadmanage.model.FonteFinanciadora;
import edu.uea.acadmanage.model.AuditLog;
import edu.uea.acadmanage.repository.FonteFinanciadoraRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FonteFinanciadoraService {

        private final FonteFinanciadoraRepository fonteFinanciadoraRepository;
        private final AuditLogService auditLogService;
        private final ObjectMapper objectMapper;

        public FonteFinanciadoraService(FonteFinanciadoraRepository fonteFinanciadoraRepository, 
                                        UsuarioRepository usuarioRepository, 
                                        AuditLogService auditLogService, 
                                        ObjectMapper objectMapper) {
                this.fonteFinanciadoraRepository = fonteFinanciadoraRepository;
                this.auditLogService = auditLogService;
                this.objectMapper = objectMapper;
        }
        
        public List<FonteFinanciadora> listarTodasFontesFinanciadoras() {
                return fonteFinanciadoraRepository.findAll();
        }

        public FonteFinanciadora recuperarFinanciadoraPorId(Long financiadoraId) {
                FonteFinanciadora fonteFinanciadora = fonteFinanciadoraRepository.findById(financiadoraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fonte financiadora não encontrada com o ID: " + financiadoraId));
                return fonteFinanciadora;

        }


        public FonteFinanciadora salvar(FonteFinanciadora fontefinanciadora) {
                FonteFinanciadora fonteSalva = fonteFinanciadoraRepository.save(fontefinanciadora);
                
                // CAMADA 2: Audit Log
                auditLogService.log(
                    AuditLog.AuditAction.CREATE,
                    "FonteFinanciadora",
                    fonteSalva.getId(),
                    null,
                    fonteSalva,
                    "Fonte financiadora criada: " + fonteSalva.getNome()
                );
                
                return fonteSalva;
        }
        
        public void deletar(Long financiadoraId) {
                if (!fonteFinanciadoraRepository.existsById(financiadoraId)) {
                    throw new RecursoNaoEncontradoException("FonteFinanciadora não encontrada com o ID: " + financiadoraId);
                }
                
                // Capturar dados para audit log antes de deletar
                FonteFinanciadora financiadora = fonteFinanciadoraRepository.findById(financiadoraId).orElse(null);
                String financiadoraNome = financiadora != null ? financiadora.getNome() : "ID: " + financiadoraId;
                
                fonteFinanciadoraRepository.deleteById(financiadoraId);
                
                // CAMADA 2: Audit Log
                if (financiadora != null) {
                    auditLogService.log(
                        AuditLog.AuditAction.DELETE,
                        "FonteFinanciadora",
                        financiadoraId,
                        financiadora,
                        null,
                        "Fonte financiadora excluída: " + financiadoraNome
                    );
                }
        }

        public FonteFinanciadora atualizar(Long financiadoraId, FonteFinanciadora novaFinanciadora) {
                FonteFinanciadora financiadoraExistente =  this.recuperarFinanciadoraPorId(financiadoraId);
                
                // Capturar estado antigo para audit log
                FonteFinanciadora oldState = copyFonteFinanciadoraForAudit(financiadoraExistente);
                
                // Atualizando os campos permitidos
                financiadoraExistente.setNome(novaFinanciadora.getNome());
        
                // Salvando no repositório
                FonteFinanciadora financiadoraAtualizada = fonteFinanciadoraRepository.save(financiadoraExistente);
                
                // CAMADA 2: Audit Log
                auditLogService.log(
                    AuditLog.AuditAction.UPDATE,
                    "FonteFinanciadora",
                    financiadoraAtualizada.getId(),
                    oldState,
                    financiadoraAtualizada,
                    "Fonte financiadora atualizada: " + financiadoraAtualizada.getNome()
                );
                
                return financiadoraAtualizada;
        }

        public Boolean existeFonteFinanciadora(Long financiadoraId) {
                return fonteFinanciadoraRepository.existsById(financiadoraId);
        }

        // Método auxiliar para copiar fonte financiadora para audit log
        private FonteFinanciadora copyFonteFinanciadoraForAudit(FonteFinanciadora fonteFinanciadora) {
            try {
                String json = objectMapper.writeValueAsString(fonteFinanciadora);
                return objectMapper.readValue(json, FonteFinanciadora.class);
            } catch (Exception e) {
                // Se falhar a cópia profunda, criar manualmente uma cópia superficial
                FonteFinanciadora copy = new FonteFinanciadora();
                copy.setId(fonteFinanciadora.getId());
                copy.setNome(fonteFinanciadora.getNome());
                return copy;
            }
        }

}