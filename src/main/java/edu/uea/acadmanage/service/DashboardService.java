package edu.uea.acadmanage.service;

import edu.uea.acadmanage.DTO.*;
import edu.uea.acadmanage.model.*;
import edu.uea.acadmanage.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final CursoRepository cursoRepository;
    private final AtividadeRepository atividadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final FonteFinanciadoraRepository fonteFinanciadoraRepository;
    private final CategoriaRepository categoriaRepository;

    public DashboardService(
            CursoRepository cursoRepository,
            AtividadeRepository atividadeRepository,
            UsuarioRepository usuarioRepository,
            PessoaRepository pessoaRepository,
            FonteFinanciadoraRepository fonteFinanciadoraRepository,
            CategoriaRepository categoriaRepository) {
        this.cursoRepository = cursoRepository;
        this.atividadeRepository = atividadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.fonteFinanciadoraRepository = fonteFinanciadoraRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public DashboardDTO obterDadosDashboard(String username) {
        try {
            // Se username for null, retornar dashboard vazio
            if (username == null || username.isBlank()) {
                return criarDashboardVazio();
            }
            
            // Buscar usuário e verificar se é administrador
            Usuario usuario = usuarioRepository.findByEmail(username)
                    .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuário não encontrado: " + username));
            
            boolean isAdmin = usuario.getRoles().stream()
                    .anyMatch(role -> role.getNome().equals("ROLE_ADMINISTRADOR"));
            
            // Se for admin, não filtra por cursos (null = todos os cursos)
            // Se não for admin, filtra por cursos associados ao usuário
            List<Long> cursoIds = null;
            if (!isAdmin) {
                cursoIds = usuario.getCursos().stream()
                        .map(Curso::getId)
                        .collect(Collectors.toList());
                
                // Se não tiver cursos associados, retorna dashboard vazio
                if (cursoIds.isEmpty()) {
                    return criarDashboardVazio();
                }
            }
            
            LocalDate agora = LocalDate.now();
            LocalDate umMesAtras = agora.minusMonths(1);
            LocalDate doisMesesAtras = agora.minusMonths(2);

            // Métricas gerais
            MetricasGeraisDTO metricasGerais = calcularMetricasGerais(umMesAtras, doisMesesAtras, cursoIds);

            // Atividades por categoria
            List<AtividadePorCategoriaDTO> atividadesPorCategoria = calcularAtividadesPorCategoria(cursoIds);

            // Status de publicação
            StatusPublicacaoDTO statusPublicacao = calcularStatusPublicacao(cursoIds);

            // Distribuição de usuários (só admin vê)
            List<DistribuicaoUsuarioDTO> distribuicaoUsuarios = isAdmin ? calcularDistribuicaoUsuarios() : new ArrayList<>();

            // Cursos em destaque (top 4)
            List<CursoDestaqueDTO> cursosDestaque = calcularCursosDestaque(4, cursoIds);

            // Atividades recentes
            List<AtividadeRecenteDTO> atividadesRecentes = calcularAtividadesRecentes(6, cursoIds);

            return new DashboardDTO(
                    metricasGerais,
                    atividadesPorCategoria,
                    statusPublicacao,
                    distribuicaoUsuarios,
                    cursosDestaque,
                    atividadesRecentes,
                    new ArrayList<>()
            );
        } catch (org.springframework.dao.DataAccessException e) {
            // Capturar erros de acesso ao banco de dados (ex: problemas com queries)
            // Retornar dashboard vazio em vez de erro 500
            return criarDashboardVazio();
        } catch (Exception e) {
            // Em caso de qualquer outro erro (ex: tabelas vazias, problemas de query), retornar dashboard vazio
            // Isso evita que o frontend receba um erro 500
            return criarDashboardVazio();
        }
    }
    
    private DashboardDTO criarDashboardVazio() {
        MetricasGeraisDTO metricasVazias = new MetricasGeraisDTO(
                new MetricaDTO(0.0, 0L, ""),
                new MetricaDTO(0.0, 0L, ""),
                new MetricaDTO(0.0, 0L, ""),
                new MetricaDTO(0.0, 0L, ""),
                new MetricaDTO(0.0, 0L, ""),
                new MetricaDTO(0.0, 0L, ""),
                new MetricaDTO(0.0, 0L, "")
        );
        
        return new DashboardDTO(
                metricasVazias,
                new ArrayList<>(),
                new StatusPublicacaoDTO(0L, 0L, 0.0),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    private MetricasGeraisDTO calcularMetricasGerais(LocalDate umMesAtras, LocalDate doisMesesAtras, List<Long> cursoIds) {
        // Total de cursos
        long totalCursos = contarCursos(cursoIds);
        long cursosMesAnterior = contarCursosCriadosAte(umMesAtras, cursoIds);
        MetricaDTO metricasCursos = calcularMetrica(totalCursos, cursosMesAnterior, "mais que o mês anterior");

        // Atividades ativas (com data de realização futura ou recente)
        long atividadesAtivas = contarAtividades(cursoIds);
        long atividadesAtivasMesAnterior = contarAtividadesAtivasAte(umMesAtras, cursoIds);
        MetricaDTO metricasAtividades = calcularMetrica(atividadesAtivas, atividadesAtivasMesAnterior, "de crescimento");

        // Usuários cadastrados (só para admin, senão conta usuários dos cursos)
        long totalUsuarios = contarUsuarios(cursoIds);
        long usuariosMesAnterior = contarUsuariosCriadosAte(umMesAtras, cursoIds);
        MetricaDTO metricasUsuarios = calcularMetrica(totalUsuarios, usuariosMesAnterior, "de aumento");

        // Pessoas cadastradas (só para admin, senão conta pessoas dos cursos)
        long totalPessoas = contarPessoas(cursoIds);
        long pessoasMesAnterior = contarPessoasCriadasAte(umMesAtras, cursoIds);
        MetricaDTO metricasPessoas = calcularMetrica(totalPessoas, pessoasMesAnterior, "de aumento");

        // Fontes financiadoras (relacionadas às atividades dos cursos)
        long totalFontes = contarFontesFinanciadoras(cursoIds);
        long fontesMesAnterior = totalFontes; // Assumindo que não temos createdAt para FonteFinanciadora
        MetricaDTO metricasFontes = calcularMetrica(totalFontes, fontesMesAnterior, "novas fontes este mês");

        // Publicações (atividades publicadas)
        long totalPublicacoes = contarPublicacoes(cursoIds);
        long publicacoesMesAnterior = contarPublicacoesAte(umMesAtras, cursoIds);
        MetricaDTO metricasPublicacoes = calcularMetrica(totalPublicacoes, publicacoesMesAnterior, "mais publicações");

        // Taxa de conclusão (estimativa: atividades finalizadas / total)
        long atividadesFinalizadas = contarAtividadesFinalizadas(cursoIds);
        long totalAtividadesParaCalculo = Math.max(atividadesAtivas, 1);
        double taxaConclusaoAtual = (atividadesFinalizadas * 100.0) / totalAtividadesParaCalculo;
        double taxaConclusaoAnterior = calcularTaxaConclusaoAnterior(umMesAtras, cursoIds);
        double diferencaTaxa = taxaConclusaoAtual - taxaConclusaoAnterior;
        MetricaDTO metricasTaxaConclusao = new MetricaDTO(
                diferencaTaxa,
                Math.round(taxaConclusaoAtual),
                "de melhoria"
        );

        return new MetricasGeraisDTO(
                metricasCursos,
                metricasAtividades,
                metricasUsuarios,
                metricasPessoas,
                metricasFontes,
                metricasPublicacoes,
                metricasTaxaConclusao
        );
    }

    private MetricaDTO calcularMetrica(long atual, long anterior, String descricao) {
        double percentual = 0.0;
        if (anterior > 0) {
            percentual = ((atual - anterior) * 100.0) / anterior;
        } else if (atual > 0) {
            percentual = 100.0; // Crescimento de 100% quando não havia dados antes
        }
        return new MetricaDTO(percentual, atual, descricao);
    }

    // Métodos auxiliares para contar entidades (filtradas por cursoIds se fornecido)
    private long contarCursos(List<Long> cursoIds) {
        if (cursoIds == null) {
            return cursoRepository.count();
        }
        return cursoIds.size();
    }
    
    private long contarCursosCriadosAte(LocalDate data, List<Long> cursoIds) {
        // Como Curso não tem createdAt, vamos assumir que todos os cursos existiam antes
        return contarCursos(cursoIds);
    }
    
    private long contarAtividades(List<Long> cursoIds) {
        if (cursoIds == null) {
            return atividadeRepository.count();
        }
        return atividadeRepository.findByCursoIds(cursoIds).size();
    }

    private long contarAtividadesAtivasAte(LocalDate data, List<Long> cursoIds) {
        try {
            if (cursoIds == null) {
                // Evitar usar findByFiltros que pode ter problemas com LOWER() em tabelas vazias
                // Usar findAll() e filtrar em memória
                List<Atividade> todasAtividades = atividadeRepository.findAll();
                if (todasAtividades == null || todasAtividades.isEmpty()) {
                    return 0L;
                }
                return todasAtividades.stream()
                        .filter(a -> a != null && a.getDataRealizacao() != null && 
                                (a.getDataRealizacao().isBefore(data) || a.getDataRealizacao().isEqual(data)))
                        .count();
            }
            return atividadeRepository.findByCursoIds(cursoIds).stream()
                    .filter(a -> a != null && a.getDataRealizacao() != null && 
                            (a.getDataRealizacao().isBefore(data) || a.getDataRealizacao().isEqual(data)))
                    .count();
        } catch (Exception e) {
            // Se houver erro na query, retornar 0
            return 0L;
        }
    }

    private long contarUsuarios(List<Long> cursoIds) {
        if (cursoIds == null) {
            return usuarioRepository.count();
        }
        // Contar usuários únicos associados aos cursos
        Set<Long> usuariosIds = new HashSet<>();
        for (Long cursoId : cursoIds) {
            Curso curso = cursoRepository.findById(cursoId).orElse(null);
            if (curso != null && curso.getUsuarios() != null) {
                curso.getUsuarios().forEach(u -> usuariosIds.add(u.getId()));
            }
        }
        return usuariosIds.size();
    }
    
    private long contarUsuariosCriadosAte(LocalDate data, List<Long> cursoIds) {
        // Por enquanto, retorna o mesmo valor que contarUsuarios
        return contarUsuarios(cursoIds);
    }

    private long contarPessoas(List<Long> cursoIds) {
        if (cursoIds == null) {
            return pessoaRepository.count();
        }
        // Contar pessoas únicas associadas aos cursos (via usuários)
        Set<Long> pessoasIds = new HashSet<>();
        for (Long cursoId : cursoIds) {
            Curso curso = cursoRepository.findById(cursoId).orElse(null);
            if (curso != null && curso.getUsuarios() != null) {
                curso.getUsuarios().forEach(u -> {
                    if (u.getPessoa() != null) {
                        pessoasIds.add(u.getPessoa().getId());
                    }
                });
            }
        }
        return pessoasIds.size();
    }
    
    private long contarPessoasCriadasAte(LocalDate data, List<Long> cursoIds) {
        // Por enquanto, retorna o mesmo valor que contarPessoas
        return contarPessoas(cursoIds);
    }
    
    private long contarFontesFinanciadoras(List<Long> cursoIds) {
        if (cursoIds == null) {
            return fonteFinanciadoraRepository.count();
        }
        // Contar fontes financiadoras únicas associadas às atividades dos cursos
        Set<Long> fontesIds = new HashSet<>();
        List<Atividade> atividades = atividadeRepository.findByCursoIds(cursoIds);
        for (Atividade atividade : atividades) {
            if (atividade.getFontesFinanciadora() != null) {
                atividade.getFontesFinanciadora().forEach(f -> fontesIds.add(f.getId()));
            }
        }
        return fontesIds.size();
    }

    private long contarPublicacoes(List<Long> cursoIds) {
        try {
            if (cursoIds == null) {
                List<Atividade> atividades = atividadeRepository.findByStatusPublicacao(true);
                return atividades != null ? atividades.size() : 0L;
            }
            return atividadeRepository.findByCursoIds(cursoIds).stream()
                    .filter(a -> a != null && Boolean.TRUE.equals(a.getStatusPublicacao()))
                    .count();
        } catch (Exception e) {
            return 0L;
        }
    }
    
    private long contarPublicacoesAte(LocalDate data, List<Long> cursoIds) {
        try {
            if (cursoIds == null) {
                // Evitar usar findByFiltros que pode ter problemas com LOWER() em tabelas vazias
                // Usar findByStatusPublicacao e filtrar em memória
                List<Atividade> atividades = atividadeRepository.findByStatusPublicacao(true);
                if (atividades == null || atividades.isEmpty()) {
                    return 0L;
                }
                return atividades.stream()
                        .filter(a -> a != null && a.getDataRealizacao() != null &&
                                (a.getDataRealizacao().isBefore(data) || a.getDataRealizacao().isEqual(data)) &&
                                Boolean.TRUE.equals(a.getStatusPublicacao()))
                        .count();
            }
            return atividadeRepository.findByCursoIds(cursoIds).stream()
                    .filter(a -> a != null && a.getDataRealizacao() != null &&
                            Boolean.TRUE.equals(a.getStatusPublicacao()) &&
                            (a.getDataRealizacao().isBefore(data) || a.getDataRealizacao().isEqual(data)))
                    .count();
        } catch (Exception e) {
            return 0L;
        }
    }

    private long contarAtividadesFinalizadas(List<Long> cursoIds) {
        try {
            LocalDate agora = LocalDate.now();
            List<Atividade> atividades = cursoIds == null ?
                    atividadeRepository.findAll() :
                    atividadeRepository.findByCursoIds(cursoIds);
            
            if (atividades == null || atividades.isEmpty()) {
                return 0L;
            }
            
            return atividades.stream()
                    .filter(a -> a != null && a.getDataFim() != null && a.getDataFim().isBefore(agora))
                    .count();
        } catch (Exception e) {
            return 0L;
        }
    }

    private double calcularTaxaConclusaoAnterior(LocalDate data, List<Long> cursoIds) {
        long atividadesFinalizadas = contarAtividadesFinalizadas(cursoIds);
        long totalAtividades = contarAtividadesAtivasAte(data, cursoIds);
        if (totalAtividades == 0) return 0.0;
        return (atividadesFinalizadas * 100.0) / totalAtividades;
    }

    private List<AtividadePorCategoriaDTO> calcularAtividadesPorCategoria(List<Long> cursoIds) {
        try {
            List<Categoria> categorias = categoriaRepository.findAll();
            if (categorias == null || categorias.isEmpty()) {
                return new ArrayList<>();
            }
            
            Map<String, Long> contadorPorCategoria = new HashMap<>();

            List<Atividade> atividades = cursoIds == null ?
                    atividadeRepository.findAll() :
                    atividadeRepository.findByCursoIds(cursoIds);
            
            if (atividades != null) {
                for (Atividade atividade : atividades) {
                    if (atividade != null && atividade.getCategoria() != null && atividade.getCategoria().getNome() != null) {
                        String nomeCategoria = atividade.getCategoria().getNome();
                        contadorPorCategoria.put(nomeCategoria,
                                contadorPorCategoria.getOrDefault(nomeCategoria, 0L) + 1);
                    }
                }
            }

            List<AtividadePorCategoriaDTO> resultado = categorias.stream()
                    .filter(cat -> cat != null && cat.getNome() != null)
                    .map(cat -> new AtividadePorCategoriaDTO(
                            cat.getNome(),
                            contadorPorCategoria.getOrDefault(cat.getNome(), 0L)
                    ))
                    .sorted((a, b) -> Long.compare(b.quantidade(), a.quantidade()))
                    .collect(Collectors.toList());

            return resultado;
        } catch (Exception e) {
            // Em caso de erro, retornar lista vazia
            return new ArrayList<>();
        }
    }

    private StatusPublicacaoDTO calcularStatusPublicacao(List<Long> cursoIds) {
        try {
            List<Atividade> atividades = cursoIds == null ?
                    atividadeRepository.findAll() :
                    atividadeRepository.findByCursoIds(cursoIds);
            
            if (atividades == null || atividades.isEmpty()) {
                return new StatusPublicacaoDTO(0L, 0L, 0.0);
            }
            
            long publicadas = atividades.stream()
                    .filter(a -> a != null && Boolean.TRUE.equals(a.getStatusPublicacao()))
                    .count();
            long naoPublicadas = atividades.size() - publicadas;
            double percentual = atividades.isEmpty() ? 0.0 :
                    (publicadas * 100.0) / atividades.size();

            return new StatusPublicacaoDTO(publicadas, naoPublicadas, percentual);
        } catch (Exception e) {
            // Em caso de erro, retornar valores zerados
            return new StatusPublicacaoDTO(0L, 0L, 0.0);
        }
    }

    private List<DistribuicaoUsuarioDTO> calcularDistribuicaoUsuarios() {
        try {
            List<DistribuicaoUsuarioDTO> distribuicao = new ArrayList<>();

            // Buscar usuários por role
            Set<Usuario> usuariosAdmin = usuarioRepository.findAllByRoleName("ROLE_ADMINISTRADOR");
            Set<Usuario> usuariosGerente = usuarioRepository.findAllByRoleName("ROLE_GERENTE");
            Set<Usuario> usuariosSecretario = usuarioRepository.findAllByRoleName("ROLE_SECRETARIO");
            Set<Usuario> usuariosCoordenador = usuarioRepository.findAllByRoleName("ROLE_COORDENADOR_ATIVIDADE");

            distribuicao.add(new DistribuicaoUsuarioDTO("Administradores", usuariosAdmin != null ? (long) usuariosAdmin.size() : 0L));
            distribuicao.add(new DistribuicaoUsuarioDTO("Gerentes", usuariosGerente != null ? (long) usuariosGerente.size() : 0L));
            distribuicao.add(new DistribuicaoUsuarioDTO("Secretários", usuariosSecretario != null ? (long) usuariosSecretario.size() : 0L));
            distribuicao.add(new DistribuicaoUsuarioDTO("Coordenadores de Atividade", usuariosCoordenador != null ? (long) usuariosCoordenador.size() : 0L));

            return distribuicao;
        } catch (Exception e) {
            // Em caso de erro, retornar lista vazia
            return new ArrayList<>();
        }
    }

    private List<CursoDestaqueDTO> calcularCursosDestaque(int limite, List<Long> cursoIds) {
        try {
            List<Curso> cursos = cursoIds == null ?
                    cursoRepository.findAll() :
                    cursoIds.stream()
                            .map(id -> cursoRepository.findById(id).orElse(null))
                            .filter(c -> c != null)
                            .collect(Collectors.toList());
            
            if (cursos == null || cursos.isEmpty()) {
                return new ArrayList<>();
            }
            
            return cursos.stream()
                    .filter(curso -> curso != null && curso.getNome() != null)
                    .map(curso -> {
                        long quantidadeAtividades = curso.getAtividades() != null ?
                                curso.getAtividades().size() : 0;
                        long quantidadeUsuarios = curso.getUsuarios() != null ?
                                curso.getUsuarios().size() : 0;
                        return new CursoDestaqueDTO(
                                curso.getNome(),
                                quantidadeAtividades,
                                quantidadeUsuarios
                        );
                    })
                    .sorted((a, b) -> Long.compare(b.quantidadeAtividades(), a.quantidadeAtividades()))
                    .limit(limite)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Em caso de erro, retornar lista vazia
            return new ArrayList<>();
        }
    }

    private List<AtividadeRecenteDTO> calcularAtividadesRecentes(int limite, List<Long> cursoIds) {
        try {
            List<AtividadeRecenteDTO> recentes = new ArrayList<>();

            // Buscar atividades recentes ordenadas por data de realização
            List<Atividade> atividades = cursoIds == null ?
                    atividadeRepository.findAll() :
                    atividadeRepository.findByCursoIds(cursoIds);
            
            if (atividades == null || atividades.isEmpty()) {
                return new ArrayList<>();
            }
            
            atividades.sort((a, b) -> {
                if (a == null || a.getDataRealizacao() == null) return 1;
                if (b == null || b.getDataRealizacao() == null) return -1;
                return b.getDataRealizacao().compareTo(a.getDataRealizacao());
            });

            LocalDateTime agora = LocalDateTime.now();

            for (Atividade atividade : atividades.stream().limit(limite * 2).toList()) {
                if (atividade != null && 
                    atividade.getDataRealizacao() != null &&
                    atividade.getNome() != null &&
                    Boolean.TRUE.equals(atividade.getStatusPublicacao())) {
                    LocalDateTime dataAtividade = atividade.getDataRealizacao()
                            .atStartOfDay()
                            .plusHours(10); // Assumir 10h como horário padrão
                    String tempoDecorrido = calcularTempoDecorrido(dataAtividade, agora);
                    recentes.add(new AtividadeRecenteDTO(
                            "Publicação",
                            "Atividade \"" + atividade.getNome() + "\" publicada",
                            dataAtividade,
                            tempoDecorrido
                    ));
                    if (recentes.size() >= limite) break;
                }
            }

            return recentes.stream()
                    .sorted((a, b) -> b.dataHora().compareTo(a.dataHora()))
                    .limit(limite)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Em caso de erro, retornar lista vazia
            return new ArrayList<>();
        }
    }

    private String calcularTempoDecorrido(LocalDateTime inicio, LocalDateTime fim) {
        long dias = ChronoUnit.DAYS.between(inicio, fim);
        long horas = ChronoUnit.HOURS.between(inicio, fim);
        long minutos = ChronoUnit.MINUTES.between(inicio, fim);

        if (dias > 0) {
            return dias + (dias == 1 ? " dia atrás" : " dias atrás");
        } else if (horas > 0) {
            return horas + (horas == 1 ? " hora atrás" : " horas atrás");
        } else {
            return minutos + (minutos <= 1 ? " minuto atrás" : " minutos atrás");
        }
    }

}

