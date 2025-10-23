package edu.uea.acadmanage.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.uea.acadmanage.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
        
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.nome = :roleName")
    Set<Usuario> findAllByRoleName(@Param("roleName") String roleName);

    // Método para buscar usuários por nome (busca parcial, case insensitive)
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.pessoa.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<Usuario> findByPessoaNomeContainingIgnoreCase(@Param("nome") String nome, Pageable pageable);

}
