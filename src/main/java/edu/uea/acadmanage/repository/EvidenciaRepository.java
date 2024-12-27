package edu.uea.acadmanage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uea.acadmanage.model.Evidencia;

public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {
    // Encontrar evidências por atividade
    List<Evidencia> findByAtividadeId(Long atividadeId);
}
