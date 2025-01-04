package edu.uea.acadmanage.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import edu.uea.acadmanage.DTO.LoginRequestDTO;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.UsuarioRepository;

@Service
public class AuthenticationService {
    private final UsuarioRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
        UsuarioRepository userRepository,
        AuthenticationManager authenticationManager
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    public Usuario authenticate(LoginRequestDTO input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.username(),
                        input.password()
                )
        );

        return userRepository.findByEmail(input.username())
                .orElseThrow();
    }
}
