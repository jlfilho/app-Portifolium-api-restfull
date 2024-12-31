package edu.uea.acadmanage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.uea.acadmanage.model.FonteFinanciadora;
import edu.uea.acadmanage.repository.FonteFinanciadoraRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class FonteFinanciadoraService {

        private final FonteFinanciadoraRepository fonteFinanciadoraRepository;

        public FonteFinanciadoraService(FonteFinanciadoraRepository fonteFinanciadoraRepository, UsuarioRepository usuarioRepository) {
                this.fonteFinanciadoraRepository = fonteFinanciadoraRepository;
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
                return fonteFinanciadoraRepository.save(fontefinanciadora);
        }
        
        public void deletar(Long financiadoraId) {
                if (!fonteFinanciadoraRepository.existsById(financiadoraId)) {
                    throw new RecursoNaoEncontradoException("FonteFinanciadora não encontrada com o ID: " + financiadoraId);
                }
                fonteFinanciadoraRepository.deleteById(financiadoraId);
        }

        public FonteFinanciadora atualizar(Long financiadoraId, FonteFinanciadora novaFinanciadora) {
                FonteFinanciadora financiadoraExistente =  this.recuperarFinanciadoraPorId(financiadoraId);
                // Atualizando os campos permitidos
                financiadoraExistente.setNome(novaFinanciadora.getNome());
        
                // Salvando no repositório
                return fonteFinanciadoraRepository.save(financiadoraExistente);
        }

        public Boolean existeFonteFinanciadora(Long financiadoraId) {
                return fonteFinanciadoraRepository.existsById(financiadoraId);
        }


}