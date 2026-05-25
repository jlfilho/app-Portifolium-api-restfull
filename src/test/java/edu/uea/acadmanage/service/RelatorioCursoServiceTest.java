package edu.uea.acadmanage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import edu.uea.acadmanage.config.FileStorageProperties;
import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.Categoria;
import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.Evidencia;
import edu.uea.acadmanage.model.FonteFinanciadora;
import edu.uea.acadmanage.model.Role;
import edu.uea.acadmanage.model.TipoCurso;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.AtividadePessoaPapelRepository;
import edu.uea.acadmanage.repository.AtividadeRepository;
import edu.uea.acadmanage.repository.CursoRepository;
import edu.uea.acadmanage.repository.EvidenciaRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class RelatorioCursoServiceTest {

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private AtividadeRepository atividadeRepository;

    @Mock
    private EvidenciaRepository evidenciaRepository;

    @Mock
    private AtividadePessoaPapelRepository atividadePessoaPapelRepository;

    @Mock
    private CursoService cursoService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @TempDir
    Path tempDir;

    private RelatorioCursoService relatorioCursoService;

    @BeforeEach
    void setUp() throws IOException {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setStorageLocation(tempDir.toString());

        TemplateEngine templateEngine = criarTemplateEngine();

        relatorioCursoService = new RelatorioCursoService(
                cursoRepository,
                atividadeRepository,
                evidenciaRepository,
                atividadePessoaPapelRepository,
                cursoService,
                usuarioRepository,
                templateEngine,
                properties);
    }

    @Test
    void deveGerarPdfComSucesso() throws IOException {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setNome("Curso de Teste");
        TipoCurso tipoCurso = new TipoCurso();
        tipoCurso.setNome("Especialização");
        curso.setTipoCurso(tipoCurso);

        Categoria categoria = new Categoria();
        categoria.setId(10L);
        categoria.setNome("Extensão");

        Atividade atividade = new Atividade();
        atividade.setId(100L);
        atividade.setNome("Oficina de Inovação");
        atividade.setObjetivo("Estimular projetos.");
        atividade.setPublicoAlvo("Comunidade acadêmica");
        atividade.setStatusPublicacao(true);
        atividade.setDataRealizacao(LocalDate.of(2024, 6, 1));
        atividade.setCategoria(categoria);
        atividade.setCurso(curso);
        FonteFinanciadora fonte = new FonteFinanciadora();
        fonte.setNome("FAPEAM");
        atividade.setFontesFinanciadora(List.of(fonte));

        Evidencia evidencia = new Evidencia();
        evidencia.setId(200L);
        evidencia.setLegenda("Registro fotográfico");
        evidencia.setOrdem(1);
        evidencia.setUrlFoto("foto.jpg");
        evidencia.setAtividade(atividade);

        List<Long> categoriasFiltro = List.of(categoria.getId());
        LocalDate dataInicio = LocalDate.of(2024, 5, 1);
        LocalDate dataFim = LocalDate.of(2024, 7, 1);
        String solicitante = "usuario@teste.com";

        // Criar usuário mock com role ADMINISTRADOR
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(solicitante);
        Role roleAdmin = new Role();
        roleAdmin.setId(1L);
        roleAdmin.setNome("ROLE_ADMINISTRADOR");
        Set<Role> roles = new HashSet<>();
        roles.add(roleAdmin);
        usuario.setRoles(roles);
        
        when(cursoRepository.findById(curso.getId())).thenReturn(Optional.of(curso));
        when(usuarioRepository.findByEmail(solicitante)).thenReturn(Optional.of(usuario));
        when(atividadeRepository.findByCursoId(curso.getId()))
                .thenReturn(List.of(atividade));
        when(evidenciaRepository.findByAtividadeIdsOrderByAtividadeAndOrdem(anyList()))
                .thenReturn(List.of(evidencia));
        // Mock para contar participantes da atividade
        Object[] resultadoParticipantes = new Object[]{atividade.getId(), 5L};
        when(atividadePessoaPapelRepository.countParticipantesByAtividadeIds(anyList(), anyList()))
                .thenReturn(List.<Object[]>of(resultadoParticipantes));

        Path evidenciasDir = tempDir.resolve("evidencias");
        Files.createDirectories(evidenciasDir);
        Files.write(evidenciasDir.resolve("foto.jpg"), "conteudo".getBytes(StandardCharsets.UTF_8));

        byte[] pdf = relatorioCursoService.gerarRelatorioCurso(
                curso.getId(),
                dataInicio,
                dataFim,
                categoriasFiltro,
                "Introdução do relatório",
                solicitante);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0, "PDF deve possuir conteúdo");
        String cabecalho = new String(pdf, 0, 4, StandardCharsets.ISO_8859_1);
        assertEquals("%PDF", cabecalho);
        assertThat(pdf.length).isGreaterThan(100);
    }

    private TemplateEngine criarTemplateEngine() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver);
        return engine;
    }
}

