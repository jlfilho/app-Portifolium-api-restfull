package edu.uea.acadmanage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.uea.acadmanage.model.Evidencia;

public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {
    List<Evidencia> findByAtividadeIdOrderByOrdemAsc(Long atividadeId);

    @Query("SELECT COALESCE(MAX(e.ordem), -1) FROM Evidencia e WHERE e.atividade.id = :atividadeId")
    Integer findMaxOrdemByAtividadeId(@Param("atividadeId") Long atividadeId);
}
