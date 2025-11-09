package edu.uea.acadmanage.DTO;

import jakarta.validation.constraints.NotBlank;

public record UnidadeAcademicaDTO(
        Long id,
        @NotBlank(message = "O nome da unidade acadêmica é obrigatório.")
        String nome,
        String descricao
) {}

