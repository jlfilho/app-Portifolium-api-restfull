package edu.uea.acadmanage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uea.acadmanage.model.Pessoa;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    Optional<Pessoa> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}
