package edu.uea.acadmanage.DTO;

public record PermissaoCursoDTO(
        Long cursoId,
        Long usuarioId,
        String usuarioNome,
        String permissao
        ) {}