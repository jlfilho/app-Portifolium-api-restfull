package edu.uea.acadmanage.DTO;

import java.util.List;

public record DashboardDTO(
        MetricasGeraisDTO metricasGerais,
        List<AtividadePorCategoriaDTO> atividadesPorCategoria,
        StatusPublicacaoDTO statusPublicacao,
        List<DistribuicaoUsuarioDTO> distribuicaoUsuarios,
        List<CursoDestaqueDTO> cursosDestaque,
        List<AtividadeRecenteDTO> atividadesRecentes,
        List<MetaProgressoDTO> metasProgresso
) {}

