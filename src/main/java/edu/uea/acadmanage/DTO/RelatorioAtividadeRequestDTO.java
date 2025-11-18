package edu.uea.acadmanage.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RelatorioAtividadeRequestDTO(
        String introducao
) {
}

