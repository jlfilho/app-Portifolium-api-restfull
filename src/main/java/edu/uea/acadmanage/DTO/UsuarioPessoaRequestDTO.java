package edu.uea.acadmanage.DTO;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioPessoaRequestDTO(
        @NotNull(message = "O ID da pessoa é obrigatório.")
        Long pessoaId,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, message = "A senha deve conter pelo menos 6 caracteres.")
        String senha,

        @NotBlank(message = "A role é obrigatória.")
        String role,

        List<Long> cursosIds) {
}

