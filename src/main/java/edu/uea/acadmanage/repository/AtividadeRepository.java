package edu.uea.acadmanage.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.uea.acadmanage.model.Atividade;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
  // Encontrar atividades por curso
  List<Atividade> findByCursoId(Long cursoId);

  // Encontrar atividades por status de publicação
  List<Atividade> findByStatusPublicacao(Boolean statusPublicacao);

  // Para gerentes e secretários: Retorna atividades de cursos específicos
  @Query("SELECT a FROM Atividade a WHERE a.curso.id IN :cursoIds")
  List<Atividade> findByCursoIds(@Param("cursoIds") List<Long> cursoIds);

  // Para gerentes e secretários: Retorna atividades de cursos específicos por id
  @Query("""
      SELECT a
      FROM Atividade a
      WHERE a.id = :atividadeId
        AND a.curso.id IN :cursoIds
      """)
  Optional<Atividade> findByIdAndCursoIds(@Param("atividadeId") Long atividadeId,
      @Param("cursoIds") List<Long> cursoIds);

  // Para gerentes e secretários: Retorna atividades de cursos específicos por id
  @Query("""
          SELECT a
          FROM Atividade a
          WHERE (:cursoNome IS NULL OR LOWER(a.curso.nome) LIKE LOWER(CONCAT('%', :cursoNome, '%')))
            AND (:categoriaNome IS NULL OR LOWER(a.categoria.nome) LIKE LOWER(CONCAT('%', :categoriaNome, '%')))
            AND (:dataInicio IS NULL OR 
                 (a.dataFim IS NULL AND a.dataRealizacao >= :dataInicio) OR
                 (a.dataFim IS NOT NULL AND a.dataFim >= :dataInicio))
            AND (:dataFim IS NULL OR 
                 (a.dataFim IS NULL AND a.dataRealizacao <= :dataFim) OR
                 (a.dataFim IS NOT NULL AND a.dataRealizacao <= :dataFim))
            AND a.curso.id IN :cursoIds
      """)
  List<Atividade> findByFiltros2(
      @Param("cursoNome") String cursoNome,
      @Param("categoriaNome") String categoriaNome,
      @Param("dataInicio") LocalDate dataInicio,
      @Param("dataFim") LocalDate dataFim,
      @Param("cursoIds") List<Long> cursoIds);

  @Query("""
          SELECT a FROM Atividade a
          WHERE (:cursoId IS NULL OR a.curso.id = :cursoId)
            AND (:categoriaId IS NULL OR a.categoria.id = :categoriaId)
            AND (:nome IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
            AND (:dataInicio IS NULL OR 
                 (a.dataFim IS NULL AND a.dataRealizacao >= :dataInicio) OR
                 (a.dataFim IS NOT NULL AND a.dataFim >= :dataInicio))
            AND (:dataFim IS NULL OR 
                 (a.dataFim IS NULL AND a.dataRealizacao <= :dataFim) OR
                 (a.dataFim IS NOT NULL AND a.dataRealizacao <= :dataFim))
            AND (:statusPublicacao IS NULL OR a.statusPublicacao = :statusPublicacao)
      """)
  List<Atividade> findByFiltros(
      @Param("cursoId") Long cursoId,
      @Param("categoriaId") Long categoriaId,
      @Param("nome") String nome,
      @Param("dataInicio") LocalDate dataInicio,
      @Param("dataFim") LocalDate dataFim,
      @Param("statusPublicacao") Boolean statusPublicacao);

  @Query("""
          SELECT a FROM Atividade a
          WHERE (:cursoId IS NULL OR a.curso.id = :cursoId)
            AND (:categoriaId IS NULL OR a.categoria.id = :categoriaId)
            AND (:nome IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
            AND (:dataInicio IS NULL OR 
                 (a.dataFim IS NULL AND a.dataRealizacao >= :dataInicio) OR
                 (a.dataFim IS NOT NULL AND a.dataFim >= :dataInicio))
            AND (:dataFim IS NULL OR 
                 (a.dataFim IS NULL AND a.dataRealizacao <= :dataFim) OR
                 (a.dataFim IS NOT NULL AND a.dataRealizacao <= :dataFim))
            AND (:statusPublicacao IS NULL OR a.statusPublicacao = :statusPublicacao)
      """)
  Page<Atividade> findByFiltrosPaginado(
      @Param("cursoId") Long cursoId,
      @Param("categoriaId") Long categoriaId,
      @Param("nome") String nome,
      @Param("dataInicio") LocalDate dataInicio,
      @Param("dataFim") LocalDate dataFim,
      @Param("statusPublicacao") Boolean statusPublicacao,
      Pageable pageable);

  long countByCategoriaId(Long categoriaId);

  @Query("""
      SELECT a
      FROM Atividade a
      WHERE a.curso.id = :cursoId
        AND (:dataInicio IS NULL OR 
             (a.dataFim IS NULL AND a.dataRealizacao >= :dataInicio) OR
             (a.dataFim IS NOT NULL AND a.dataFim >= :dataInicio))
        AND (:dataFim IS NULL OR 
             (a.dataFim IS NULL AND a.dataRealizacao <= :dataFim) OR
             (a.dataFim IS NOT NULL AND a.dataRealizacao <= :dataFim))
        AND (:categorias IS NULL OR a.categoria.id IN :categorias)
        AND a.statusPublicacao = TRUE
      ORDER BY a.categoria.nome ASC, a.dataRealizacao ASC, a.nome ASC
      """)
  List<Atividade> findForRelatorio(
      @Param("cursoId") Long cursoId,
      @Param("dataInicio") LocalDate dataInicio,
      @Param("dataFim") LocalDate dataFim,
      @Param("categorias") List<Long> categorias);

}
