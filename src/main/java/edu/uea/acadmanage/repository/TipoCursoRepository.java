package edu.uea.acadmanage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uea.acadmanage.model.TipoCurso;

public interface TipoCursoRepository extends JpaRepository<TipoCurso, Long> {
    Optional<TipoCurso> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}


