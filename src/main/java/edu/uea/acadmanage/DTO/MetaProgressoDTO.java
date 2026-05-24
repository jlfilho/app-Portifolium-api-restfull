package edu.uea.acadmanage.DTO;

public record MetaProgressoDTO(
        String nome,
        Long atual,
        Long meta,
        Double percentual
) {}

