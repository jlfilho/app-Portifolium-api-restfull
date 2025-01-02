package edu.uea.acadmanage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uea.acadmanage.model.Pessoa;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    
}
