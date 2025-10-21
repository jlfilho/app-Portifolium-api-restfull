package edu.uea.acadmanage.DTO;

import java.time.LocalDate;

public record AtividadeFiltroDTO(
        Long cursoId,
        Long categoriaId,
        String nome,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean statusPublicacao
) {}
