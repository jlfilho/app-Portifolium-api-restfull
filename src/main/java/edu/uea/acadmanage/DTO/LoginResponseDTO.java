package edu.uea.acadmanage.DTO;

public record LoginResponseDTO(
    String token,
    long expiresIn
) {}