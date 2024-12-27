package edu.uea.acadmanage.DTO;

import java.util.List;

public record CategoriaDTO(
        Long id,
        String nome,
        List<AtividadeDTO> atividades) {
}
