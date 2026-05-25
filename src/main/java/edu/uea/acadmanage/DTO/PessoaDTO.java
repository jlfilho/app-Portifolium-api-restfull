package edu.uea.acadmanage.DTO;

import jakarta.validation.constraints.NotBlank;

public record PessoaDTO(
        Long id,
        @NotBlank(message = "O nome da pessoa é obrigatório.")
        String nome,
        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,
        boolean possuiUsuario
) {}
