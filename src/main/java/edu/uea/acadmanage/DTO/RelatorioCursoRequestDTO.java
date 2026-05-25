package edu.uea.acadmanage.DTO;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RelatorioCursoRequestDTO(
        LocalDate dataInicio,
        LocalDate dataFim,
        List<Long> categorias,
        String introducao
) {
}
