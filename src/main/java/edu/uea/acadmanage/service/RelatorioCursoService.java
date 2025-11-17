package edu.uea.acadmanage.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import edu.uea.acadmanage.config.FileStorageProperties;
import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.Categoria;
import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.Evidencia;
import edu.uea.acadmanage.model.FonteFinanciadora;
import edu.uea.acadmanage.model.Papel;
import edu.uea.acadmanage.model.UnidadeAcademica;
import edu.uea.acadmanage.repository.AtividadePessoaPapelRepository;
import edu.uea.acadmanage.repository.AtividadeRepository;
import edu.uea.acadmanage.repository.CursoRepository;
import edu.uea.acadmanage.repository.EvidenciaRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

import javax.imageio.ImageIO;

@Service
public class RelatorioCursoService {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(LOCALE_PT_BR);
    private static final Logger LOGGER = LoggerFactory.getLogger(RelatorioCursoService.class);

    private final CursoRepository cursoRepository;
    private final AtividadeRepository atividadeRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final AtividadePessoaPapelRepository atividadePessoaPapelRepository;
    private final CursoService cursoService;
    private final TemplateEngine templateEngine;
    private final Path evidenciasBasePath;

    public RelatorioCursoService(
            CursoRepository cursoRepository,
            AtividadeRepository atividadeRepository,
            EvidenciaRepository evidenciaRepository,
            AtividadePessoaPapelRepository atividadePessoaPapelRepository,
            CursoService cursoService,
            TemplateEngine templateEngine,
            FileStorageProperties fileStorageProperties) throws IOException {
        this.cursoRepository = cursoRepository;
        this.atividadeRepository = atividadeRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.atividadePessoaPapelRepository = atividadePessoaPapelRepository;
        this.cursoService = cursoService;
        this.templateEngine = templateEngine;
        this.evidenciasBasePath = Paths.get(fileStorageProperties.getStorageLocation())
                .resolve("evidencias")
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(this.evidenciasBasePath);
    }

    @Transactional(readOnly = true)
    public byte[] gerarRelatorioCurso(Long cursoId,
                                      LocalDate dataInicio,
                                      LocalDate dataFim,
                                      List<Long> categorias,
                                      String introducao,
                                      String solicitanteEmail) {
        if (solicitanteEmail == null || solicitanteEmail.isBlank()) {
            throw new AcessoNegadoException("Usuário não autenticado.");
        }

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));

        if (!cursoService.verificarAcessoAoCurso(solicitanteEmail, cursoId)) {
            throw new AcessoNegadoException("Usuário não possui permissão para gerar o relatório deste curso.");
        }

        List<Long> categoriasFiltro = (categorias == null || categorias.isEmpty()) ? null : categorias;
        List<Atividade> atividades = atividadeRepository.findForRelatorio(cursoId, dataInicio, dataFim, categoriasFiltro);

        List<Long> atividadeIds = atividades.stream()
                .map(Atividade::getId)
                .toList();

        Map<Long, List<Evidencia>> evidenciasMap = atividadeIds.isEmpty()
                ? Collections.emptyMap()
                : evidenciaRepository.findByAtividadeIdsOrderByAtividadeAndOrdem(atividadeIds).stream()
                        .collect(Collectors.groupingBy(e -> e.getAtividade().getId(), LinkedHashMap::new, Collectors.toList()));

        Map<Long, Integer> participantesMap = carregarParticipantesPorAtividade(atividadeIds);

        RelatorioDados dados = agruparDados(curso, atividades, evidenciasMap, participantesMap);

        String html = renderizarHtmlRelatorio(dados, dataInicio, dataFim, introducao);
        return gerarPdf(html);
    }

    private RelatorioDados agruparDados(Curso curso,
                                        List<Atividade> atividades,
                                        Map<Long, List<Evidencia>> evidenciasMap,
                                        Map<Long, Integer> participantesMap) {
        Map<Categoria, List<RelatorioAtividade>> categoriasAgrupadas = new LinkedHashMap<>();
        int totalEvidencias = 0;
        int totalParticipantes = 0;

        for (Atividade atividade : atividades) {
            Categoria categoria = atividade.getCategoria();
            Categoria chave = categoriasAgrupadas.keySet().stream()
                    .filter(cat -> Objects.equals(cat.getId(), categoria.getId()))
                    .findFirst()
                    .orElse(categoria);

            List<Evidencia> evidencias = Optional.ofNullable(evidenciasMap.get(atividade.getId()))
                    .orElseGet(List::of);
            totalEvidencias += evidencias.size();

            int participantes = participantesMap.getOrDefault(atividade.getId(), 0);
            RelatorioAtividade relatorioAtividade = new RelatorioAtividade(atividade, evidencias, participantes);
            totalParticipantes += participantes;

            categoriasAgrupadas.computeIfAbsent(chave, key -> new ArrayList<>())
                    .add(relatorioAtividade);
        }

        int totalAtividades = atividades.size();

        List<RelatorioCategoria> categoriasOrdenadas = categoriasAgrupadas.entrySet().stream()
                .map(entry -> new RelatorioCategoria(entry.getKey(), entry.getValue()))
                .toList();

        return new RelatorioDados(curso, categoriasOrdenadas, totalAtividades, totalEvidencias, totalParticipantes);
    }

    private String renderizarHtmlRelatorio(RelatorioDados dados,
                                           LocalDate dataInicio,
                                           LocalDate dataFim,
                                           String introducao) {
        String cursoNome = Optional.ofNullable(dados.curso().getNome()).orElse("Curso sem nome");
        String tipoCurso = Optional.ofNullable(dados.curso().getTipoCurso())
                .map(tipo -> Optional.ofNullable(tipo.getNome()).orElse(""))
                .filter(nome -> !nome.isBlank())
                .orElse(null);
        String tituloCurso = tipoCurso != null ? cursoNome + " — " + tipoCurso : cursoNome;
        String tipoCursoNome = Optional.ofNullable(dados.curso().getTipoCurso())
                .map(tipo -> normalizarTexto(tipo.getNome()))
                .orElse(null);
        String unidadeAcademicaNome = Optional.ofNullable(dados.curso().getUnidadeAcademica())
                .map(UnidadeAcademica::getNome)
                .map(this::normalizarTexto)
                .orElse(null);
        String cursoDescricao = normalizarTexto(dados.curso().getDescricao());

        String periodo = construirPeriodo(dataInicio, dataFim);
        String introducaoSanitizada = normalizarTexto(introducao);
        List<String> introducaoParagrafos = dividirParagrafos(introducaoSanitizada);
        ViewTotais totais = new ViewTotais(dados.totalAtividades(), dados.totalEvidencias(), dados.totalParticipantes());
        List<ViewCategoria> viewCategorias = mapearCategorias(dados.categorias());

        Context ctx = new Context(LOCALE_PT_BR);
        ctx.setVariable("tituloPagina", "Relatório de Atividades");
        ctx.setVariable("tituloCurso", tituloCurso);
        ctx.setVariable("cursoNome", cursoNome);
        ctx.setVariable("tipoCurso", tipoCursoNome);
        ctx.setVariable("unidadeAcademica", unidadeAcademicaNome);
        ctx.setVariable("descricaoCurso", cursoDescricao);
        ctx.setVariable("periodo", periodo);
        ctx.setVariable("introducao", introducaoSanitizada);
        ctx.setVariable("introducaoParagrafos", introducaoParagrafos);
        ctx.setVariable("totais", totais);
        ctx.setVariable("categorias", viewCategorias);

        return templateEngine.process("relatorio/relatorio-curso", ctx);
    }

    private List<ViewCategoria> mapearCategorias(List<RelatorioCategoria> categorias) {
        if (categorias == null || categorias.isEmpty()) {
            return List.of();
        }
        return categorias.stream()
                .map(this::mapearCategoria)
                .toList();
    }

    private ViewCategoria mapearCategoria(RelatorioCategoria categoria) {
        String nomeCategoria = Optional.ofNullable(categoria.categoria().getNome()).orElse("Categoria sem nome");
        List<ViewAtividade> atividades = categoria.atividades().stream()
                .map(this::mapearAtividade)
                .toList();
        return new ViewCategoria(
                nomeCategoria,
                atividades.size(),
                categoria.totalEvidencias(),
                categoria.totalParticipantes(),
                atividades
        );
    }

    private ViewAtividade mapearAtividade(RelatorioAtividade atividade) {
        String status = Boolean.TRUE.equals(atividade.getStatusPublicacao()) ? "Publicado" : "Não publicado";
        String coordenador = normalizarTexto(atividade.getCoordenadorNome());
        List<String> fontes = atividade.getFontesFinanciadorasNomes();
        List<ViewEvidencia> evidencias = mapearEvidencias(atividade.getEvidencias());

        return new ViewAtividade(
                atividade.getNome(),
                normalizarTexto(atividade.getObjetivo()),
                normalizarTexto(atividade.getPublicoAlvo()),
                DATE_FORMATTER.format(atividade.getDataRealizacao()),
                status,
                coordenador,
                fontes,
                atividade.quantidadeParticipantes(),
                evidencias
        );
    }

    private List<ViewEvidencia> mapearEvidencias(List<Evidencia> evidencias) {
        if (evidencias == null || evidencias.isEmpty()) {
            return List.of();
        }

        List<Evidencia> evidenciasOrdenadas = new ArrayList<>(evidencias);
        evidenciasOrdenadas.sort((a, b) -> {
            int ordemCompare = Integer.compare(Optional.ofNullable(a.getOrdem()).orElse(0), Optional.ofNullable(b.getOrdem()).orElse(0));
            if (ordemCompare != 0) {
                return ordemCompare;
            }
            return Long.compare(Optional.ofNullable(a.getId()).orElse(0L), Optional.ofNullable(b.getId()).orElse(0L));
        });

        return evidenciasOrdenadas.stream()
                .map(this::mapearEvidencia)
                .toList();
    }

    private ViewEvidencia mapearEvidencia(Evidencia evidencia) {
        String legenda = Optional.ofNullable(evidencia.getLegenda())
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .orElse("Sem legenda");
        String orientacao = determinarOrientacao(evidencia);
        return new ViewEvidencia(construirDataUriEvidencia(evidencia), legenda, orientacao);
    }


    private String normalizarTexto(String valor) {
        return Optional.ofNullable(valor)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .orElse(null);
    }

    private List<String> dividirParagrafos(String texto) {
        if (texto == null) {
            return List.of();
        }
        String normalized = texto.replace("\r\n", "\n");
        String[] blocos = normalized.split("\\n\\s*\\n");
        return Arrays.stream(blocos)
                .map(String::trim)
                .filter(str -> !str.isBlank())
                .map(par -> par.replace("\n", " "))
                .toList();
    }

    private Map<Long, Integer> carregarParticipantesPorAtividade(List<Long> atividadeIds) {
        if (atividadeIds == null || atividadeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Papel> papeisParticipantes = new ArrayList<>(EnumSet.of(Papel.PARTICIPANTE, Papel.BOLSISTA, Papel.VOLUNTARIO, Papel.COORDENADOR));
        List<Object[]> resultados = atividadePessoaPapelRepository.countParticipantesByAtividadeIds(atividadeIds, papeisParticipantes);
        Map<Long, Integer> participantes = new HashMap<>(resultados.size());
        for (Object[] linha : resultados) {
            Long atividadeId = (Long) linha[0];
            Long quantidade = (Long) linha[1];
            participantes.put(atividadeId, quantidade != null ? quantidade.intValue() : 0);
        }
        return participantes;
    }

    private String construirPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null || dataFim != null) {
            StringBuilder periodo = new StringBuilder("Período: ");
            if (dataInicio != null) {
                periodo.append(DATE_FORMATTER.format(dataInicio));
            }
            periodo.append(" até ");
            if (dataFim != null) {
                periodo.append(DATE_FORMATTER.format(dataFim));
            }
            return periodo.toString();
        }
        return "Período não informado";
    }

    private String construirDataUriEvidencia(Evidencia evidencia) {
        if (evidencia.getUrlFoto() == null || evidencia.getUrlFoto().isBlank()) {
            return null;
        }

        try {
            Path imagePath = resolveEvidenciaPath(evidencia.getUrlFoto());
            if (!Files.exists(imagePath)) {
                return null;
            }

            byte[] bytes = Files.readAllBytes(imagePath);
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String contentType = Optional.ofNullable(Files.probeContentType(imagePath))
                    .orElse("image/jpeg");
            return "data:" + contentType + ";base64," + base64;
        } catch (IOException ex) {
            return null;
        }
    }

    private String determinarOrientacao(Evidencia evidencia) {
        if (evidencia.getUrlFoto() == null || evidencia.getUrlFoto().isBlank()) {
            return "desconhecida";
        }

        try {
            Path imagePath = resolveEvidenciaPath(evidencia.getUrlFoto());
            if (!Files.exists(imagePath)) {
                return "desconhecida";
            }

            try (InputStream input = Files.newInputStream(imagePath)) {
                BufferedImage imagem = ImageIO.read(input);
                if (imagem == null) {
                    return "desconhecida";
                }
                int largura = imagem.getWidth();
                int altura = imagem.getHeight();
                if (largura <= 0 || altura <= 0) {
                    return "desconhecida";
                }
                if (largura > altura) {
                    return "horizontal";
                } else if (altura > largura) {
                    return "vertical";
                } else {
                    return "quadrada";
                }
            }
        } catch (IOException ex) {
            return "desconhecida";
        }
    }

    private Path resolveEvidenciaPath(String urlFoto) {
        String normalized = urlFoto.replace('\\', '/');
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("evidencias/")) {
            normalized = normalized.substring("evidencias/".length());
        }
        return evidenciasBasePath.resolve(normalized).normalize();
    }

    private byte[] gerarPdf(String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, resolverBaseUri());
            configurarPdfA(builder);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            LOGGER.error("Falha ao transformar HTML em PDF. Conteúdo HTML (parcial): {}", resumirHtml(html));
            throw new IllegalStateException("Erro ao gerar o relatório em PDF", e);
        }
    }

    private String resumirHtml(String html) {
        if (html == null) {
            return "[null]";
        }
        String sanitized = html.replaceAll("\\s+", " ").trim();
        return sanitized.length() > 300 ? sanitized.substring(0, 300) + "..." : sanitized;
    }
    private void configurarPdfA(PdfRendererBuilder builder) throws IOException {
        URL iccUrl = getClass().getResource("/icc/sRGB.icc");
        if (iccUrl == null) {
            return;
        }
        try (InputStream iccStream = iccUrl.openStream()) {
            byte[] profileBytes = iccStream.readAllBytes();
            builder.useColorProfile(profileBytes);
            builder.usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_1_B);
        }
    }

    private String resolverBaseUri() {
        URL baseUrl = getClass().getResource("/pdf/");
        return baseUrl != null ? baseUrl.toExternalForm() : null;
    }

    private record RelatorioDados(Curso curso,
                                  List<RelatorioCategoria> categorias,
                                  int totalAtividades,
                                  int totalEvidencias,
                                  int totalParticipantes) {
    }

    private record RelatorioCategoria(Categoria categoria,
                                      List<RelatorioAtividade> atividades) {
        int totalEvidencias() {
            return atividades.stream()
                    .mapToInt(RelatorioAtividade::totalEvidencias)
                    .sum();
        }

        int totalParticipantes() {
            return atividades.stream()
                    .mapToInt(RelatorioAtividade::quantidadeParticipantes)
                    .sum();
        }
    }

    private record RelatorioAtividade(Atividade atividade,
                                      List<Evidencia> evidencias,
                                      int participantes) {
        String getNome() {
            return Optional.ofNullable(atividade.getNome()).orElse("Atividade sem nome");
        }

        String getObjetivo() {
            return Optional.ofNullable(atividade.getObjetivo()).orElse(null);
        }

        String getPublicoAlvo() {
            return Optional.ofNullable(atividade.getPublicoAlvo()).orElse(null);
        }

        LocalDate getDataRealizacao() {
            return Optional.ofNullable(atividade.getDataRealizacao()).orElse(LocalDate.now());
        }

        Boolean getStatusPublicacao() {
            return Optional.ofNullable(atividade.getStatusPublicacao()).orElse(Boolean.FALSE);
        }

        List<Evidencia> getEvidencias() {
            if (evidencias == null) {
                return List.of();
            }
            return evidencias;
        }

        int totalEvidencias() {
            return getEvidencias().size();
        }

        String getCoordenadorNome() {
            String coordenador = atividade.getCoordenador();
            return coordenador != null && !coordenador.isBlank() ? coordenador : null;
        }

        List<String> getFontesFinanciadorasNomes() {
            List<FonteFinanciadora> fontes = atividade.getFontesFinanciadora();
            if (fontes == null) {
                return List.of();
            }
            return fontes.stream()
                    .map(FonteFinanciadora::getNome)
                    .filter(Objects::nonNull)
                    .filter(nome -> !nome.isBlank())
                    .toList();
        }

        int quantidadeParticipantes() {
            return participantes;
        }
    }

    private record ViewTotais(int totalAtividades,
                              int totalEvidencias,
                              int totalParticipantes) {
    }

    private record ViewEvidencia(String imgDataUri,
                                 String legenda,
                                 String orientacao) {
    }

    private record ViewAtividade(String nome,
                                 String objetivo,
                                 String publicoAlvo,
                                 String data,
                                 String status,
                                 String coordenador,
                                 List<String> fontes,
                                 int qtdParticipantes,
                                 List<ViewEvidencia> evidencias) {
    }

    private record ViewCategoria(String nome,
                                 int qtdAtividades,
                                 int qtdEvidencias,
                                 int qtdParticipantes,
                                 List<ViewAtividade> atividades) {
    }
}
