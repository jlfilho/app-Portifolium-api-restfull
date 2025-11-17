package edu.uea.acadmanage.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EvidenciaOrdemDTO(
        @NotNull(message = "O ID da evidência é obrigatório.")
        Long evidenciaId,
        @NotNull(message = "A ordem é obrigatória.")
        @Min(value = 0, message = "A ordem deve ser um número não negativo.")
        Integer ordem
) {}

