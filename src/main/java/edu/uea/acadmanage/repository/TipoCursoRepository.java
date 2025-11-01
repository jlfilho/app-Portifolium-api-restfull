package edu.uea.acadmanage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uea.acadmanage.model.TipoCurso;
import edu.uea.acadmanage.model.TipoCursoCodigo;

public interface TipoCursoRepository extends JpaRepository<TipoCurso, Long> {
    Optional<TipoCurso> findByCodigo(TipoCursoCodigo codigo);
    boolean existsByCodigo(TipoCursoCodigo codigo);
    boolean existsByNomeIgnoreCase(String nome);
}


