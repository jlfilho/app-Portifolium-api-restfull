package edu.uea.acadmanage.DTO;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record AtividadeFiltroDTO(
        @NotBlank(message = "O id do curso não pode ser vazio.")
        Long cursoId,
        Long categoriaId,
        String nome,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean statusPublicacao
) {}
