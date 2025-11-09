package edu.uea.acadmanage.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.uea.acadmanage.model.UnidadeAcademica;

public interface UnidadeAcademicaRepository extends JpaRepository<UnidadeAcademica, Long> {

    Page<UnidadeAcademica> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    boolean existsByNomeIgnoreCase(String nome);
}

