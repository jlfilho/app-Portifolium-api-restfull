package edu.uea.acadmanage.DTO;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtividadeDTO(
        Long id,
        @NotBlank(message = "O nome da atividade não pode ser vazio.")
        String nome,
        String objetivo,
        String publicoAlvo,
        @NotNull(message = "O status de publicação deve ser informado.")
        Boolean statusPublicacao,
        @NotNull(message = "A data de realização deve ser informada.")
        LocalDate dataRealizacao,
        @NotNull(message = "O ID do curso deve ser informado.")
        Long cursoId,
        @NotNull(message = "O ID da categoria deve ser informado.")
        Long categoriaId
) {}
