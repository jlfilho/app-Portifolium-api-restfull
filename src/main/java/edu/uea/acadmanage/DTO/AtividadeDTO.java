package edu.uea.acadmanage.DTO;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.uea.acadmanage.model.Categoria;
import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.FonteFinanciadora;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtividadeDTO(
        Long id,
        @NotBlank(message = "O nome da atividade não pode ser vazio.")
        String nome,
        String objetivo,
        String publicoAlvo,
        @NotNull(message = "O status de publicação deve ser informado.")
        Boolean statusPublicacao,
        String fotoCapa,
        String coordenador,
        @NotNull(message = "A data de realização deve ser informada.")
        LocalDate dataRealizacao,
        @JsonIgnoreProperties({"atividades","usuarios"})
        @NotNull(message = "O ID do curso deve ser informado.")
        Curso curso,
        @JsonIgnoreProperties({"atividades"})
        @NotNull(message = "O ID da categoria deve ser informado.")
        Categoria categoria,
        List<FonteFinanciadora> fontesFinanciadora,
        List<PessoaPapelDTO> integrantes
) {}
