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
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com o ID: " + categoriaId));
                return new CategoriaResumidaDTO(categoria.getId(), categoria.getNome());

        }


        public List<CategoriaDTO> getCategoriasComAtividadesByUsuario(String email) {
                Usuario usuario = usuarioRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

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
                                                atividade.getDataRealizacao(),
                                                atividade.getCurso().getId(),
                                                atividade.getCategoria().getId())).
                                                toList())).
                                                toList();
        }

        public Categoria salvar(Categoria categoria) {
                if (categoria.getNome() == null || categoria.getNome().isEmpty()) {
                    throw new IllegalArgumentException("O nome da categoria não pode ser vazio.");
                }
                return categoriaRepository.save(categoria);
        }
        
        public void deletar(Long categoriaId) {
                if (!categoriaRepository.existsById(categoriaId)) {
                    throw new IllegalArgumentException("Categoria não encontrada com o ID: " + categoriaId);
                }
                categoriaRepository.deleteById(categoriaId);
        }

        public Categoria atualizar(Long categoriaId, Categoria novaCategoria) {
                Categoria categoriaExistente = categoriaRepository.findById(categoriaId)
                        .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com o ID: " + categoriaId));
                // Atualizando os campos permitidos
                categoriaExistente.setNome(novaCategoria.getNome());
        
                // Salvando no repositório
                return categoriaRepository.save(categoriaExistente);
        }

        public boolean verificarSeCategoriaExiste(Long categoriaId) {
                return categoriaRepository.existsById(categoriaId);
        }
}