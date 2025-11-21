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

    @Query(value = """
            SELECT c.* FROM curso c
            WHERE (:ativo IS NULL OR c.ativo = :ativo)
              AND (:nome IS NULL OR LOWER(c.nome::text) LIKE LOWER('%' || :nome || '%'))
              AND (:tipoId IS NULL OR c.tipo_curso_id = :tipoId)
              AND (:unidadeId IS NULL OR c.unidade_academica_id = :unidadeId)
            """,
            countQuery = """
            SELECT COUNT(*) FROM curso c
            WHERE (:ativo IS NULL OR c.ativo = :ativo)
              AND (:nome IS NULL OR LOWER(c.nome::text) LIKE LOWER('%' || :nome || '%'))
              AND (:tipoId IS NULL OR c.tipo_curso_id = :tipoId)
              AND (:unidadeId IS NULL OR c.unidade_academica_id = :unidadeId)
            """,
            nativeQuery = true)
    Page<Curso> findAllByFiltros(@Param("ativo") Boolean ativo,
            @Param("nome") String nome,
            @Param("tipoId") Long tipoId,
            @Param("unidadeId") Long unidadeId,
            Pageable pageable);

    @Query(value = """
            SELECT c.* FROM curso c
            INNER JOIN curso_usuario cu ON c.id = cu.curso_id
            WHERE cu.usuario_id = :usuarioId
              AND (:ativo IS NULL OR c.ativo = :ativo)
              AND (:nome IS NULL OR LOWER(c.nome::text) LIKE LOWER('%' || :nome || '%'))
              AND (:tipoId IS NULL OR c.tipo_curso_id = :tipoId)
              AND (:unidadeId IS NULL OR c.unidade_academica_id = :unidadeId)
            """, 
            countQuery = """
            SELECT COUNT(*) FROM curso c
            INNER JOIN curso_usuario cu ON c.id = cu.curso_id
            WHERE cu.usuario_id = :usuarioId
              AND (:ativo IS NULL OR c.ativo = :ativo)
              AND (:nome IS NULL OR LOWER(c.nome::text) LIKE LOWER('%' || :nome || '%'))
              AND (:tipoId IS NULL OR c.tipo_curso_id = :tipoId)
              AND (:unidadeId IS NULL OR c.unidade_academica_id = :unidadeId)
            """,
            nativeQuery = true)
    Page<Curso> findByUsuarioAndFiltros(@Param("usuarioId") Long usuarioId,
            @Param("ativo") Boolean ativo,
            @Param("nome") String nome,
            @Param("tipoId") Long tipoId,
            @Param("unidadeId") Long unidadeId,
            Pageable pageable);

}
