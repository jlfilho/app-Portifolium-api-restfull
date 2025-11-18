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
        if (cursoIds == null) {
            return atividadeRepository.findByFiltros(null, null, null, null, data, null).size();
        }
        return atividadeRepository.findByCursoIds(cursoIds).stream()
                .filter(a -> a.getDataRealizacao().isBefore(data) || a.getDataRealizacao().isEqual(data))
                .count();
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
        if (cursoIds == null) {
            return atividadeRepository.findByStatusPublicacao(true).size();
        }
        return atividadeRepository.findByCursoIds(cursoIds).stream()
                .filter(a -> Boolean.TRUE.equals(a.getStatusPublicacao()))
                .count();
    }
    
    private long contarPublicacoesAte(LocalDate data, List<Long> cursoIds) {
        if (cursoIds == null) {
            List<Atividade> atividades = atividadeRepository.findByFiltros(
                    null, null, null, null, data, true);
            return atividades.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getStatusPublicacao()))
                    .count();
        }
        return atividadeRepository.findByCursoIds(cursoIds).stream()
                .filter(a -> Boolean.TRUE.equals(a.getStatusPublicacao()) &&
                        (a.getDataRealizacao().isBefore(data) || a.getDataRealizacao().isEqual(data)))
                .count();
    }

    private long contarAtividadesFinalizadas(List<Long> cursoIds) {
        LocalDate agora = LocalDate.now();
        List<Atividade> atividades = cursoIds == null ?
                atividadeRepository.findByFiltros(null, null, null, null, agora, null) :
                atividadeRepository.findByCursoIds(cursoIds);
        return atividades.stream()
                .filter(a -> a.getDataFim() != null && a.getDataFim().isBefore(agora))
                .count();
    }

    private double calcularTaxaConclusaoAnterior(LocalDate data, List<Long> cursoIds) {
        long atividadesFinalizadas = contarAtividadesFinalizadas(cursoIds);
        long totalAtividades = contarAtividadesAtivasAte(data, cursoIds);
        if (totalAtividades == 0) return 0.0;
        return (atividadesFinalizadas * 100.0) / totalAtividades;
    }

    private List<AtividadePorCategoriaDTO> calcularAtividadesPorCategoria(List<Long> cursoIds) {
        List<Categoria> categorias = categoriaRepository.findAll();
        Map<String, Long> contadorPorCategoria = new HashMap<>();

        List<Atividade> atividades = cursoIds == null ?
                atividadeRepository.findAll() :
                atividadeRepository.findByCursoIds(cursoIds);
        
        for (Atividade atividade : atividades) {
            String nomeCategoria = atividade.getCategoria().getNome();
            contadorPorCategoria.put(nomeCategoria,
                    contadorPorCategoria.getOrDefault(nomeCategoria, 0L) + 1);
        }

        List<AtividadePorCategoriaDTO> resultado = categorias.stream()
                .map(cat -> new AtividadePorCategoriaDTO(
                        cat.getNome(),
                        contadorPorCategoria.getOrDefault(cat.getNome(), 0L)
                ))
                .sorted((a, b) -> Long.compare(b.quantidade(), a.quantidade()))
                .collect(Collectors.toList());

        return resultado;
    }

    private StatusPublicacaoDTO calcularStatusPublicacao(List<Long> cursoIds) {
        List<Atividade> atividades = cursoIds == null ?
                atividadeRepository.findAll() :
                atividadeRepository.findByCursoIds(cursoIds);
        
        long publicadas = atividades.stream()
                .filter(a -> Boolean.TRUE.equals(a.getStatusPublicacao()))
                .count();
        long naoPublicadas = atividades.size() - publicadas;
        double percentual = atividades.isEmpty() ? 0.0 :
                (publicadas * 100.0) / atividades.size();

        return new StatusPublicacaoDTO(publicadas, naoPublicadas, percentual);
    }

    private List<DistribuicaoUsuarioDTO> calcularDistribuicaoUsuarios() {
        List<DistribuicaoUsuarioDTO> distribuicao = new ArrayList<>();

        // Buscar usuários por role
        Set<Usuario> usuariosAdmin = usuarioRepository.findAllByRoleName("ROLE_ADMINISTRADOR");
        Set<Usuario> usuariosGerente = usuarioRepository.findAllByRoleName("ROLE_GERENTE");
        Set<Usuario> usuariosSecretario = usuarioRepository.findAllByRoleName("ROLE_SECRETARIO");
        Set<Usuario> usuariosCoordenador = usuarioRepository.findAllByRoleName("ROLE_COORDENADOR_ATIVIDADE");

        distribuicao.add(new DistribuicaoUsuarioDTO("Administradores", (long) usuariosAdmin.size()));
        distribuicao.add(new DistribuicaoUsuarioDTO("Gerentes", (long) usuariosGerente.size()));
        distribuicao.add(new DistribuicaoUsuarioDTO("Secretários", (long) usuariosSecretario.size()));
        distribuicao.add(new DistribuicaoUsuarioDTO("Coordenadores de Atividade", (long) usuariosCoordenador.size()));
        


        return distribuicao;
    }

    private List<CursoDestaqueDTO> calcularCursosDestaque(int limite, List<Long> cursoIds) {
        List<Curso> cursos = cursoIds == null ?
                cursoRepository.findAll() :
                cursoIds.stream()
                        .map(id -> cursoRepository.findById(id).orElse(null))
                        .filter(c -> c != null)
                        .collect(Collectors.toList());
        
        return cursos.stream()
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
    }

    private List<AtividadeRecenteDTO> calcularAtividadesRecentes(int limite, List<Long> cursoIds) {
        List<AtividadeRecenteDTO> recentes = new ArrayList<>();

        // Buscar atividades recentes ordenadas por data de realização
        List<Atividade> atividades = cursoIds == null ?
                atividadeRepository.findAll() :
                atividadeRepository.findByCursoIds(cursoIds);
        
        atividades.sort((a, b) -> b.getDataRealizacao().compareTo(a.getDataRealizacao()));

        LocalDateTime agora = LocalDateTime.now();

        for (Atividade atividade : atividades.stream().limit(limite * 2).toList()) {
            if (Boolean.TRUE.equals(atividade.getStatusPublicacao())) {
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

