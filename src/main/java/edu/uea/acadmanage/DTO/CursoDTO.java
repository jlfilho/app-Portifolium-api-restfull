package edu.uea.acadmanage.DTO;

public record CursoDTO(
        Long id,
        String nome,
        String descricao,
        String fotoCapa,
        Boolean ativo,
        Long tipoId
) {}

