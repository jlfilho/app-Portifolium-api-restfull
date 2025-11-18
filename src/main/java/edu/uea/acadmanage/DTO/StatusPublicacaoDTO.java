package edu.uea.acadmanage.DTO;

public record StatusPublicacaoDTO(
        Long publicadas,
        Long naoPublicadas,
        Double percentualPublicadas
) {}

