package edu.uea.acadmanage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.uea.acadmanage.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    @Query("SELECT c FROM Curso c JOIN c.usuarios u WHERE u.id = :usuarioId")
    List<Curso> findCursosByUsuarioId(@Param("usuarioId") Long usuarioId);

    

}
