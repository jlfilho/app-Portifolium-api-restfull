package edu.uea.acadmanage.service;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.uea.acadmanage.DTO.AtividadeDTO;
import edu.uea.acadmanage.DTO.CategoriaDTO;
import edu.uea.acadmanage.DTO.CategoriaResumidaDTO;
import edu.uea.acadmanage.DTO.PessoaPapelDTO;
import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.Categoria;
import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.CategoriaRepository;
import edu.uea.acadmanage.repository.AtividadeRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import edu.uea.acadmanage.service.exception.ConflitoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class CategoriaService {

        private final CategoriaRepository categoriaRepository;
        private final UsuarioRepository usuarioRepository;
        private final AtividadeRepository atividadeRepository;

        public CategoriaService(CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository, AtividadeRepository atividadeRepository) {
                this.categoriaRepository = categoriaRepository;
                this.usuarioRepository = usuarioRepository;
                this.atividadeRepository = atividadeRepository;
        }
        
        public List<CategoriaResumidaDTO> listarTodasCategorias() {
                return categoriaRepository.findAll().stream()
                        .map(categoria -> new CategoriaResumidaDTO(
                                categoria.getId(), 
                                categoria.getNome()))
                        .toList();
        }

        // Listar categorias com paginação
        public Page<CategoriaResumidaDTO> listarTodasCategoriasPaginadas(Pageable pageable) {
                return categoriaRepository.findAll(pageable)
                        .map(categoria -> new CategoriaResumidaDTO(
                                categoria.getId(), 
                                categoria.getNome()));
        }

        public CategoriaResumidaDTO recuperarCategoriaPorId(Long categoriaId) {
                Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com o ID: " + categoriaId));
                return new CategoriaResumidaDTO(categoria.getId(), categoria.getNome());

        }

        // Consulta categorias de atividades por curso, com possibilidade de filtrar por uma, mais de uma ou todas.
        public List<CategoriaDTO> getCategoriasPorCurso(Long cursoId, List<Long> categorias, Boolean statusPublicacao, String nomeAtividade) {
                // Obtém categorias e filtra atividades diretamente no Stream
                return categoriaRepository.findCategoriasPorCurso(cursoId, categorias, statusPublicacao, nomeAtividade).stream()
                        .map(categoria -> {
                            // Filtra atividades para garantir que pertencem ao curso especificado
                            List<AtividadeDTO> atividadesFiltradas = categoria.getAtividades().stream()
                                    .filter(atividade -> cursoId.equals(atividade.getCurso().getId()))
                                    .map(this::toAtividadeDTO) // Converte para DTO
                                    .toList();
            
                            // Apenas inclui categorias com atividades após o filtro
                            return atividadesFiltradas.isEmpty() ? null : toCategoriaDTO(categoria, atividadesFiltradas);
                        })
                        .filter(Objects::nonNull) // Remove categorias vazias
                        .toList();
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
                                                atividade.getCoordenador(),
                                                atividade.getDataRealizacao(),
                                                atividade.getCurso(),
                                                atividade.getCategoria(),
                                                atividade.getFontesFinanciadora(),
                                                atividade.getPessoas().stream().map(p -> new PessoaPapelDTO(p.getPessoa().getId(), p.getPessoa().getNome(), p.getPessoa().getCpf(), p.getPapel().name())).toList())).
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
                long numAtividades = atividadeRepository.countByCategoriaId(categoriaId);
                if (numAtividades > 0) {
                    throw new ConflitoException("Não é possível excluir a categoria. Existem " + numAtividades + " atividade(s) associada(s).");
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

        // Converte uma entidade Categoria para CategoriaDTO
private CategoriaDTO toCategoriaDTO(Categoria categoria, List<AtividadeDTO> atividades) {
        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNome(),
                atividades
        );
    }
    
    // Converte uma entidade Atividade para AtividadeDTO
    private AtividadeDTO toAtividadeDTO(Atividade atividade) {
        return new AtividadeDTO(
                atividade.getId(),
                atividade.getNome(),
                atividade.getObjetivo(),
                atividade.getPublicoAlvo(),
                atividade.getStatusPublicacao(),
                atividade.getFotoCapa(),
                atividade.getCoordenador(),
                atividade.getDataRealizacao(),
                atividade.getCurso(),
                atividade.getCategoria(),
                atividade.getFontesFinanciadora(),
                atividade.getPessoas().stream().map(p -> new PessoaPapelDTO(p.getPessoa().getId(), p.getPessoa().getNome(), p.getPessoa().getCpf(), p.getPapel().name())).toList()
        );
    }
}