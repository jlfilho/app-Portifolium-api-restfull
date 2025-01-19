package edu.uea.acadmanage.DTO;

import jakarta.validation.constraints.NotBlank;

public record CursoDTO(
        Long id,
        @NotBlank(message = "O nome do curso não pode ser vazio.")
        String nome,
        Boolean ativo
) {}

