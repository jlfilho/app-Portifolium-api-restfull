package edu.uea.acadmanage.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.uea.acadmanage.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    @Query("SELECT c FROM Curso c JOIN c.usuarios u WHERE u.id = :usuarioId")
    List<Curso> findCursosByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT c FROM Curso c JOIN c.usuarios u WHERE u.id = :usuarioId")
    Page<Curso> findCursosByUsuarioIdPaginado(@Param("usuarioId") Long usuarioId, Pageable pageable);

    // Existência de cursos por tipo (para validação de exclusão de tipo)
    boolean existsByTipoCurso_Id(Long tipoId);

    boolean existsByUnidadeAcademica_Id(Long unidadeId);

    @Query("""
            SELECT c FROM Curso c
            WHERE (:ativo IS NULL OR c.ativo = :ativo)
              AND (:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
              AND (:tipoId IS NULL OR c.tipoCurso.id = :tipoId)
              AND (:unidadeId IS NULL OR c.unidadeAcademica.id = :unidadeId)
            """)
    Page<Curso> findAllByFiltros(@Param("ativo") Boolean ativo,
            @Param("nome") String nome,
            @Param("tipoId") Long tipoId,
            @Param("unidadeId") Long unidadeId,
            Pageable pageable);

    @Query("""
            SELECT c FROM Curso c JOIN c.usuarios u
            WHERE u.id = :usuarioId
              AND (:ativo IS NULL OR c.ativo = :ativo)
              AND (:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
              AND (:tipoId IS NULL OR c.tipoCurso.id = :tipoId)
              AND (:unidadeId IS NULL OR c.unidadeAcademica.id = :unidadeId)
            """)
    Page<Curso> findByUsuarioAndFiltros(@Param("usuarioId") Long usuarioId,
            @Param("ativo") Boolean ativo,
            @Param("nome") String nome,
            @Param("tipoId") Long tipoId,
            @Param("unidadeId") Long unidadeId,
            Pageable pageable);

}
