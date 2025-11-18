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
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
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
import edu.uea.acadmanage.repository.EvidenciaRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

import javax.imageio.ImageIO;

@Service
public class RelatorioAtividadeService {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(LOCALE_PT_BR);
    private static final Logger LOGGER = LoggerFactory.getLogger(RelatorioAtividadeService.class);

    private final AtividadeRepository atividadeRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final AtividadePessoaPapelRepository atividadePessoaPapelRepository;
    private final AtividadeAutorizacaoService atividadeAutorizacaoService;
    private final TemplateEngine templateEngine;
    private final Path evidenciasBasePath;

    public RelatorioAtividadeService(
            AtividadeRepository atividadeRepository,
            EvidenciaRepository evidenciaRepository,
            AtividadePessoaPapelRepository atividadePessoaPapelRepository,
            AtividadeAutorizacaoService atividadeAutorizacaoService,
            TemplateEngine templateEngine,
            FileStorageProperties fileStorageProperties) throws IOException {
        this.atividadeRepository = atividadeRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.atividadePessoaPapelRepository = atividadePessoaPapelRepository;
        this.atividadeAutorizacaoService = atividadeAutorizacaoService;
        this.templateEngine = templateEngine;
        this.evidenciasBasePath = Paths.get(fileStorageProperties.getStorageLocation())
                .resolve("evidencias")
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(this.evidenciasBasePath);
    }

    @Transactional(readOnly = true)
    public byte[] gerarRelatorioAtividade(Long atividadeId,
                                          String introducao,
                                          String solicitanteEmail) {
        if (solicitanteEmail == null || solicitanteEmail.isBlank()) {
            throw new AcessoNegadoException("Usuário não autenticado.");
        }

        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada com o ID: " + atividadeId));

        // Verificar se o usuário tem permissão para gerar o relatório desta atividade
        if (!atividadeAutorizacaoService.podeEditarAtividade(solicitanteEmail, atividadeId)) {
            throw new AcessoNegadoException("Usuário não possui permissão para gerar o relatório desta atividade.");
        }

        // Buscar evidências da atividade
        List<Evidencia> evidencias = evidenciaRepository.findByAtividadeIdOrderByOrdemAsc(atividadeId);

        // Buscar quantidade de participantes
        int participantes = carregarParticipantesPorAtividade(atividadeId);

        // Preparar dados para o relatório
        RelatorioDados dados = prepararDados(atividade, evidencias, participantes);

        // Renderizar HTML
        String html = renderizarHtmlRelatorio(dados, introducao);

        // Gerar PDF
        return gerarPdf(html);
    }

    private RelatorioDados prepararDados(Atividade atividade, List<Evidencia> evidencias, int participantes) {
        return new RelatorioDados(atividade, evidencias, participantes);
    }

    private String renderizarHtmlRelatorio(RelatorioDados dados, String introducao) {
        Atividade atividade = dados.atividade();
        Curso curso = atividade.getCurso();
        Categoria categoria = atividade.getCategoria();

        // Informações do curso
        String cursoNome = Optional.ofNullable(curso.getNome()).orElse("Curso sem nome");
        String tipoCurso = Optional.ofNullable(curso.getTipoCurso())
                .map(tipo -> Optional.ofNullable(tipo.getNome()).orElse(""))
                .filter(nome -> !nome.isBlank())
                .orElse(null);
        String tituloCurso = tipoCurso != null ? cursoNome + " — " + tipoCurso : cursoNome;
        String tipoCursoNome = Optional.ofNullable(curso.getTipoCurso())
                .map(tipo -> normalizarTexto(tipo.getNome()))
                .orElse(null);
        String unidadeAcademicaNome = Optional.ofNullable(curso.getUnidadeAcademica())
                .map(UnidadeAcademica::getNome)
                .map(this::normalizarTexto)
                .orElse(null);
        String cursoDescricao = normalizarTexto(curso.getDescricao());

        // Informações da atividade
        String atividadeNome = Optional.ofNullable(atividade.getNome()).orElse("Atividade sem nome");
        String categoriaNome = Optional.ofNullable(categoria.getNome()).orElse("Categoria sem nome");
        String atividadeObjetivo = normalizarTexto(atividade.getObjetivo());
        String atividadePublicoAlvo = normalizarTexto(atividade.getPublicoAlvo());
        String coordenador = normalizarTexto(atividade.getCoordenador());
        String dataFormatada = formatarDataAtividade(atividade.getDataRealizacao(), atividade.getDataFim());
        String status = Boolean.TRUE.equals(atividade.getStatusPublicacao()) ? "Publicado" : "Não publicado";
        
        List<String> fontes = atividade.getFontesFinanciadora() != null
                ? atividade.getFontesFinanciadora().stream()
                        .map(FonteFinanciadora::getNome)
                        .filter(Objects::nonNull)
                        .filter(nome -> !nome.isBlank())
                        .collect(Collectors.toList())
                : List.of();

        // Introdução
        String introducaoSanitizada = normalizarTexto(introducao);
        List<String> introducaoParagrafos = dividirParagrafos(introducaoSanitizada);

        // Evidências
        List<ViewEvidencia> viewEvidencias = mapearEvidencias(dados.evidencias());

        // Preparar contexto para o template
        Context ctx = new Context(LOCALE_PT_BR);
        ctx.setVariable("tituloPagina", "Relatório de Atividade");
        ctx.setVariable("tituloCurso", tituloCurso);
        ctx.setVariable("cursoNome", cursoNome);
        ctx.setVariable("tipoCurso", tipoCursoNome);
        ctx.setVariable("unidadeAcademica", unidadeAcademicaNome);
        ctx.setVariable("descricaoCurso", cursoDescricao);
        ctx.setVariable("atividadeNome", atividadeNome);
        ctx.setVariable("categoriaNome", categoriaNome);
        ctx.setVariable("atividadeObjetivo", atividadeObjetivo);
        ctx.setVariable("atividadePublicoAlvo", atividadePublicoAlvo);
        ctx.setVariable("coordenador", coordenador);
        ctx.setVariable("dataFormatada", dataFormatada);
        ctx.setVariable("status", status);
        ctx.setVariable("fontes", fontes);
        ctx.setVariable("participantes", dados.participantes());
        ctx.setVariable("totalEvidencias", dados.evidencias().size());
        ctx.setVariable("introducao", introducaoSanitizada);
        ctx.setVariable("introducaoParagrafos", introducaoParagrafos);
        ctx.setVariable("evidencias", viewEvidencias);

        return templateEngine.process("relatorio/relatorio-atividade", ctx);
    }

    private String formatarDataAtividade(LocalDate dataRealizacao, LocalDate dataFim) {
        if (dataFim == null) {
            return DATE_FORMATTER.format(dataRealizacao);
        } else {
            return DATE_FORMATTER.format(dataRealizacao) + " a " + DATE_FORMATTER.format(dataFim);
        }
    }

    private List<ViewEvidencia> mapearEvidencias(List<Evidencia> evidencias) {
        if (evidencias == null || evidencias.isEmpty()) {
            return List.of();
        }

        List<Evidencia> evidenciasOrdenadas = new ArrayList<>(evidencias);
        evidenciasOrdenadas.sort((a, b) -> {
            int ordemCompare = Integer.compare(
                    Optional.ofNullable(a.getOrdem()).orElse(0),
                    Optional.ofNullable(b.getOrdem()).orElse(0));
            if (ordemCompare != 0) {
                return ordemCompare;
            }
            return Long.compare(
                    Optional.ofNullable(a.getId()).orElse(0L),
                    Optional.ofNullable(b.getId()).orElse(0L));
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

    private int carregarParticipantesPorAtividade(Long atividadeId) {
        List<Papel> papeisParticipantes = new ArrayList<>(EnumSet.of(
                Papel.PARTICIPANTE, Papel.BOLSISTA, Papel.VOLUNTARIO, Papel.COORDENADOR));
        Long count = atividadePessoaPapelRepository.countByAtividadeIdAndPapelIn(atividadeId, papeisParticipantes);
        return count != null ? count.intValue() : 0;
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

    private record RelatorioDados(Atividade atividade,
                                  List<Evidencia> evidencias,
                                  int participantes) {
    }

    private record ViewEvidencia(String imgDataUri,
                                 String legenda,
                                 String orientacao) {
    }
}

