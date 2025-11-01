package edu.uea.acadmanage.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.TipoCursoCodigo;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    @Query("SELECT c FROM Curso c JOIN c.usuarios u WHERE u.id = :usuarioId")
    List<Curso> findCursosByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT c FROM Curso c JOIN c.usuarios u WHERE u.id = :usuarioId")
    Page<Curso> findCursosByUsuarioIdPaginado(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT c FROM Curso c JOIN c.usuarios u WHERE u.id = :usuarioId AND c.ativo = :ativo")
    Page<Curso> findCursosByUsuarioIdAndAtivo(@Param("usuarioId") Long usuarioId, @Param("ativo") Boolean ativo, Pageable pageable);

    @Query("SELECT c FROM Curso c JOIN c.usuarios u WHERE u.id = :usuarioId AND LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<Curso> findCursosByUsuarioIdAndNomeContaining(@Param("usuarioId") Long usuarioId, @Param("nome") String nome, Pageable pageable);

    @Query("SELECT c FROM Curso c JOIN c.usuarios u WHERE u.id = :usuarioId AND c.ativo = :ativo AND LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<Curso> findCursosByUsuarioIdAndAtivoAndNomeContaining(@Param("usuarioId") Long usuarioId, @Param("ativo") Boolean ativo, @Param("nome") String nome, Pageable pageable);

    // Buscar cursos por status ativo com paginação
    Page<Curso> findByAtivo(Boolean ativo, Pageable pageable);

    // Buscar cursos por nome contendo texto (case insensitive) com paginação
    Page<Curso> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    // Buscar cursos por status e nome com paginação
    Page<Curso> findByAtivoAndNomeContainingIgnoreCase(Boolean ativo, String nome, Pageable pageable);

    // Filtros por tipo de curso (navegando por associação tipoCurso.codigo)
    Page<Curso> findByTipoCurso_Codigo(TipoCursoCodigo codigo, Pageable pageable);
    Page<Curso> findByAtivoAndTipoCurso_Codigo(Boolean ativo, TipoCursoCodigo codigo, Pageable pageable);
    Page<Curso> findByNomeContainingIgnoreCaseAndTipoCurso_Codigo(String nome, TipoCursoCodigo codigo, Pageable pageable);
    Page<Curso> findByAtivoAndNomeContainingIgnoreCaseAndTipoCurso_Codigo(Boolean ativo, String nome, TipoCursoCodigo codigo, Pageable pageable);

}
