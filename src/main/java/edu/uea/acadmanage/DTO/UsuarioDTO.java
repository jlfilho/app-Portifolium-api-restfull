package edu.uea.acadmanage.DTO;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UsuarioDTO(
    Long id, 
    @NotNull(message = "O nome é obrigatório.")
    String nome, 
    @NotNull(message = "O email obrigatório.")
    @Email(message = "E-mail inválido")
    String email,
    String senha,
    String role, 
    List<CursoDTO> cursos) {}
