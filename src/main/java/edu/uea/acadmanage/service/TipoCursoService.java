package edu.uea.acadmanage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.uea.acadmanage.model.TipoCurso;
import edu.uea.acadmanage.repository.TipoCursoRepository;
import edu.uea.acadmanage.service.exception.ConflitoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class TipoCursoService {

        private final TipoCursoRepository tipoCursoRepository;

        public TipoCursoService(TipoCursoRepository tipoCursoRepository) {
                this.tipoCursoRepository = tipoCursoRepository;
        }
        
        public List<TipoCurso> listarTodos() {
                return tipoCursoRepository.findAll();
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
                return tipoCursoRepository.save(tipoCurso);
        }

        public TipoCurso atualizar(Long id, TipoCurso novo) {
                TipoCurso existente = this.recuperarPorId(id);

                if (!existente.getNome().equalsIgnoreCase(novo.getNome()) && tipoCursoRepository.existsByNomeIgnoreCase(novo.getNome())) {
                        throw new ConflitoException("Já existe um tipo de curso com o nome: " + novo.getNome());
                }

                existente.setNome(novo.getNome());
                return tipoCursoRepository.save(existente);
        }

        public void deletar(Long id) {
                if (!tipoCursoRepository.existsById(id)) {
                        throw new RecursoNaoEncontradoException("Tipo de curso não encontrado com o ID: " + id);
                }
                tipoCursoRepository.deleteById(id);
        }
}


