package edu.uea.acadmanage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.uea.acadmanage.DTO.AtividadeDTO;
import edu.uea.acadmanage.DTO.CategoriaDTO;
import edu.uea.acadmanage.DTO.CategoriaResumidaDTO;
import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.Categoria;
import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.CategoriaRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import edu.uea.acadmanage.service.exception.ConflitoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class CategoriaService {

        private final CategoriaRepository categoriaRepository;
        private final UsuarioRepository usuarioRepository;

        public CategoriaService(CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository) {
                this.categoriaRepository = categoriaRepository;
                this.usuarioRepository = usuarioRepository;
        }
        
        public List<CategoriaResumidaDTO> listarTodasCategorias() {
                return categoriaRepository.findAll().stream()
                        .map(categoria -> new CategoriaResumidaDTO(
                                categoria.getId(), 
                                categoria.getNome()))
                        .toList();
        }

        public CategoriaResumidaDTO recuperarCategoriaPorId(Long categoriaId) {
                Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com o ID: " + categoriaId));
                return new CategoriaResumidaDTO(categoria.getId(), categoria.getNome());

        }


        public List<CategoriaDTO> getCategoriasComAtividadesByUsuario(String email) {
                Usuario usuario = usuarioRepository.findByEmail(email)
                                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

                List<Long> cursoIds = usuario.getCursos().stream()
                                .map(Curso::getId)
                                .toList();

                List<Categoria> categorias = categoriaRepository.findCategoriasComAtividadesByCursoIds(cursoIds);

                // Filtrar atividades permitidas
                categorias.forEach(categoria -> {
                        List<Atividade> atividadesFiltradas = categoria.getAtividades().stream()
                                        .filter(atividade -> cursoIds.contains(atividade.getCurso().getId()))
                                        .toList();
                        categoria.setAtividades(atividadesFiltradas);
                });

                return categorias.stream()
                                .map(categoria -> new CategoriaDTO(
                                                categoria.getId(),
                                                categoria.getNome(),
                                                categoria.getAtividades().stream().map(atividade -> new AtividadeDTO(                atividade.getId(),
                                                atividade.getNome(),
                                                atividade.getObjetivo(),
                                                atividade.getPublicoAlvo(),
                                                atividade.getStatusPublicacao(),
                                                atividade.getFotoCapa(),
                                                atividade.getDataRealizacao(),
                                                atividade.getCurso().getId(),
                                                atividade.getCategoria().getId(),
                                                atividade.getFontesFinanciadora())).
                                                toList())).
                                                toList();
        }

        public Categoria salvar(Categoria categoria) {
                if (categoriaRepository.findByNomeIgnoreCase(categoria.getNome()).isPresent()) {
                    throw new AcessoNegadoException("Já existe uma categoria com o nome: " + categoria.getNome());
                }
                return categoriaRepository.save(categoria);
        }
        
        public void deletar(Long categoriaId) {
                if (!categoriaRepository.existsById(categoriaId)) {
                    throw new RecursoNaoEncontradoException("Categoria não encontrada com o ID: " + categoriaId);
                }
                categoriaRepository.deleteById(categoriaId);
        }

        public Categoria atualizar(Long categoriaId, Categoria novaCategoria) {
                Categoria categoriaExistente = categoriaRepository.findById(categoriaId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com o ID: " + categoriaId));

                if (categoriaRepository.findByNomeIgnoreCase(novaCategoria.getNome()).isPresent() && !categoriaExistente.getNome().equalsIgnoreCase(novaCategoria.getNome())) {  
                                throw new AcessoNegadoException("Já existe uma categoria com o nome: " + novaCategoria.getNome());
                            }
                // Atualizando os campos permitidos
                categoriaExistente.setNome(novaCategoria.getNome());
        
                // Salvando no repositório
                return categoriaRepository.save(categoriaExistente);
        }

        public boolean verificarSeCategoriaExiste(Long categoriaId) {
                return categoriaRepository.existsById(categoriaId);
        }
}