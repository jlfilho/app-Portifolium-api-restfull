package edu.uea.acadmanage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.uea.acadmanage.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    @Query("""
            SELECT DISTINCT c
            FROM Categoria c
            JOIN c.atividades a
            WHERE a.curso.id IN :cursoIds
            """)
    List<Categoria> findCategoriasComAtividadesByCursoIds(@Param("cursoIds") List<Long> cursoIds);

    Optional<Categoria> findByNomeIgnoreCase(String nome);
}
