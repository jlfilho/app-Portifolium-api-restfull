package edu.uea.acadmanage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uea.acadmanage.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
        
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

}
