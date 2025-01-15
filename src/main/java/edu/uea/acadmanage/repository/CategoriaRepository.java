package edu.uea.acadmanage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.uea.acadmanage.model.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    @Query("""
            SELECT DISTINCT c
            FROM Categoria c
            JOIN c.atividades a
            WHERE a.curso.id IN :cursoIds
            """)
    List<Categoria> findCategoriasComAtividadesByCursoIds(@Param("cursoIds") List<Long> cursoIds);

    Optional<Categoria> findByNomeIgnoreCase(String nome);

    @Query("SELECT DISTINCT c FROM Categoria c " +
           "JOIN c.atividades a " +
           "JOIN a.curso cr " +
           "WHERE cr.id = :cursoId " +
           "AND (:categorias IS NULL OR c.id IN :categorias) " +
           "AND (:statusPublicacao IS NULL OR a.statusPublicacao = :statusPublicacao) " +
           "AND (:nomeAtividade IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nomeAtividade, '%')))")
    List<Categoria> findCategoriasPorCurso(@Param("cursoId") Long cursoId, 
                                           @Param("categorias") List<Long> categorias,
                                           @Param("statusPublicacao") Boolean statusPublicacao,
                                           @Param("nomeAtividade") String nomeAtividade);
}
