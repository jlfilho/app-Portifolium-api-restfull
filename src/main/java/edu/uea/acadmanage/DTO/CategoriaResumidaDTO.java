package edu.uea.acadmanage.DTO;

import jakarta.validation.constraints.NotBlank;

public record CategoriaResumidaDTO(
    Long id, 
    @NotBlank(message = "O nome da categoria é obrigatório")
    String nome) {}