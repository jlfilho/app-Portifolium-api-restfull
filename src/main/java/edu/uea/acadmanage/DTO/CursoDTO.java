package edu.uea.acadmanage.DTO;

import edu.uea.acadmanage.model.TipoCursoCodigo;

public record CursoDTO(
        Long id,
        String nome,
        String descricao,
        String fotoCapa,
        Boolean ativo,
        TipoCursoCodigo tipo
) {}

