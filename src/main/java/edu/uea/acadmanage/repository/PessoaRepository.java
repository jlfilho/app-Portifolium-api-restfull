package edu.uea.acadmanage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.uea.acadmanage.model.Pessoa;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    Optional<Pessoa> findByCpf(String cpf);

    List<Pessoa> findByCpfIn(List<String> cpfs);

    Page<Pessoa> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}