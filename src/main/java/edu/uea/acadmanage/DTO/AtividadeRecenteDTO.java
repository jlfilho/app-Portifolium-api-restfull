package edu.uea.acadmanage.DTO;

import java.time.LocalDateTime;

public record AtividadeRecenteDTO(
        String tipo,
        String descricao,
        LocalDateTime dataHora,
        String tempoDecorrido
) {}

