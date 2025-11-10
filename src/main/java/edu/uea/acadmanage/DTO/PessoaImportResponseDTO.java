package edu.uea.acadmanage.DTO;

import java.util.List;

public record PessoaImportResponseDTO(
        int totalProcessados,
        int totalCadastrados,
        List<String> cadastrados,
        List<String> duplicados
) {}

