package edu.uea.acadmanage.DTO;

import jakarta.validation.constraints.NotNull;

public record LoginRequestDTO(
    @NotNull(message = "O email é obrigatório.")
    String username,
    @NotNull(message = "A senha é obrigatória.")
    String password) {}
