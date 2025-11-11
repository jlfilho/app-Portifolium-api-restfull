package edu.uea.acadmanage.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EvidenciaDTO(
        Long id,
        @NotNull(message = "O ID da atividade é obrigatório.")
        Long atividadeId,
        @NotBlank(message = "O caminho da foto não pode ser vazio.")
        String foto,
        @NotBlank(message = "A legenda não pode ser vazia.")
        String legenda,
        @NotNull(message = "A ordem da evidência é obrigatória.")
        Integer ordem,
        String criadoPor
) {}
