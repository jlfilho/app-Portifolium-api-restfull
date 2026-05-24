package edu.uea.acadmanage.DTO;

public record MetricasGeraisDTO(
        MetricaDTO totalCursos,
        MetricaDTO atividadesAtivas,
        MetricaDTO usuariosCadastrados,
        MetricaDTO pessoasCadastradas,
        MetricaDTO fontesFinanciadoras,
        MetricaDTO publicacoes,
        MetricaDTO taxaConclusao
) {}

