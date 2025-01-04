package edu.uea.acadmanage.DTO;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record CategoriaDTO(
        Long id,
        @NotBlank(message = "O nome da categoria é obrigatório")
        String nome,
        List<AtividadeDTO> atividades) {
}
