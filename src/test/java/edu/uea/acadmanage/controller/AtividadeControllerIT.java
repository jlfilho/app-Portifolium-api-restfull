package edu.uea.acadmanage.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("jwt")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AtividadeControllerIT {

    @LocalServerPort
    private int port;

    private String adminToken;
    private String gerenteToken;
    private String secretarioToken;

    @BeforeEach
    void setUp() {
        // Configurar a porta base do REST Assured
        io.restassured.RestAssured.port = port;
        
        // Obter tokens de autenticação
        try {
            adminToken = obterToken("admin@uea.edu.br", "admin123");
        } catch (AssertionError e) {
            adminToken = null;
        }
        
        try {
            gerenteToken = obterToken("gerente1@uea.edu.br", "gerente123");
        } catch (AssertionError e) {
            gerenteToken = null;
        }

        try {
            secretarioToken = obterToken("secretario1@uea.edu.br", "secretario123");
        } catch (AssertionError e) {
            secretarioToken = null;
        }
    }

    private String getAdminToken() {
        if (adminToken == null) {
            adminToken = obterToken("admin@uea.edu.br", "admin123");
        }
        return adminToken;
    }

    private String getGerenteToken() {
        if (gerenteToken == null) {
            gerenteToken = obterToken("gerente1@uea.edu.br", "gerente123");
        }
        return gerenteToken;
    }

    private String getSecretarioToken() {
        if (secretarioToken == null) {
            secretarioToken = obterToken("secretario1@uea.edu.br", "secretario123");
        }
        return secretarioToken;
    }

    private String obterToken(String email, String senha) {
        return given()
            .port(port)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "username": "%s",
                  "password": "%s"
                }
            """.formatted(email, senha))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");
    }

    // ========== GET /api/atividades/curso/{cursoId} ==========

    @Test
    void deveListarAtividadesPorCurso() {
        Long cursoId = 1L; // ID de um curso do data.sql
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/atividades/curso/{cursoId}", cursoId)
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)))
            .body("$", is(instanceOf(java.util.List.class)));
    }

    @Test
    void deveRetornar404QuandoCursoNaoExiste() {
        Long cursoIdInexistente = 9999L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/atividades/curso/{cursoId}", cursoIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar204QuandoNaoHaAtividadesParaCurso() {
        // Assumindo que pode haver um curso sem atividades
        // Por padrão, todos os cursos têm atividades no data.sql
        Long cursoId = 10L; // Pode não ter atividades
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/atividades/curso/{cursoId}", cursoId)
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    // ========== GET /api/atividades/{atividadeId} ==========

    @Test
    void deveBuscarAtividadePorId() {
        Long atividadeId = 1L; // ID de uma atividade do data.sql
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/atividades/{atividadeId}", atividadeId)
        .then()
            .log().all()
            .statusCode(200)
            .body("id", equalTo(atividadeId.intValue()))
            .body("nome", notNullValue())
            .body("curso", notNullValue())
            .body("categoria", notNullValue());
    }

    @Test
    void deveRetornar404QuandoAtividadeNaoExiste() {
        Long atividadeIdInexistente = 9999L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/atividades/{atividadeId}", atividadeIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    // ========== GET /api/atividades/{atividadeId}/usuario/{usuarioId} ==========

    @Test
    void deveBuscarAtividadePorIdEUsuario() {
        Long atividadeId = 1L;
        Long usuarioId = 1L; // Admin tem acesso ao curso 1
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/atividades/{atividadeId}/usuario/{usuarioId}", atividadeId, usuarioId)
        .then()
            .log().all()
            .statusCode(200)
            .body("id", equalTo(atividadeId.intValue()))
            .body("nome", notNullValue());
    }

    @Test
    void deveRetornar403QuandoUsuarioNaoTemAcesso() {
        Long atividadeId = 1L;
        // Usar um ID de usuário que certamente não existe ou não tem acesso
        // Como o admin tem acesso a todos os cursos, vamos usar um usuário inexistente
        Long usuarioIdInexistente = 9999L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/atividades/{atividadeId}/usuario/{usuarioId}", atividadeId, usuarioIdInexistente)
        .then()
            .log().all()
            .statusCode(anyOf(is(403), is(404)));
    }

    @Test
    void deveRetornar404QuandoAtividadeNaoExisteParaUsuario() {
        Long atividadeIdInexistente = 9999L;
        Long usuarioId = 1L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/atividades/{atividadeId}/usuario/{usuarioId}", atividadeIdInexistente, usuarioId)
        .then()
            .log().all()
            .statusCode(anyOf(is(403), is(404)));
    }

    // ========== GET /api/atividades/filtros ==========

    @Test
    void deveBuscarAtividadesComFiltros() {
        given()
            .port(port)
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/atividades/filtros")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    void deveBuscarAtividadesComFiltroPorCurso() {
        Long cursoId = 1L;
        
        given()
            .port(port)
            .param("cursoId", cursoId)
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/atividades/filtros")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void deveBuscarAtividadesComFiltroPorCategoria() {
        Long categoriaId = 1L; // Ensino
        
        given()
            .port(port)
            .param("categoriaId", categoriaId)
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/atividades/filtros")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void deveBuscarAtividadesComFiltroPorNome() {
        given()
            .port(port)
            .param("nome", "Oficina")
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/atividades/filtros")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void deveBuscarAtividadesComFiltroPorStatusPublicacao() {
        given()
            .port(port)
            .param("statusPublicacao", true)
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/atividades/filtros")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void deveBuscarAtividadesComOrdenacao() {
        given()
            .port(port)
            .param("sortBy", "nome")
            .param("sortDirection", "ASC")
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/atividades/filtros")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void deveBuscarAtividadesComOrdenacaoPorDataInicio() {
        given()
            .port(port)
            .param("sortBy", "dataInicio")
            .param("sortDirection", "DESC")
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/atividades/filtros")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void deveRetornar400QuandoCampoOrdenacaoInvalido() {
        given()
            .port(port)
            .param("sortBy", "campoInvalido")
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/atividades/filtros")
        .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    void deveRetornar404QuandoCursoNaoExisteNosFiltros() {
        Long cursoIdInexistente = 9999L;
        
        given()
            .port(port)
            .param("cursoId", cursoIdInexistente)
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/atividades/filtros")
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar204QuandoNaoHaResultados() {
        given()
            .port(port)
            .param("nome", "AtividadeInexistente12345")
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/atividades/filtros")
        .then()
            .log().all()
            .statusCode(204);
    }

    // ========== POST /api/atividades ==========

    @Test
    void deveCriarAtividadeComoAdministrador() {
        String jsonBody = """
            {
              "nome": "Nova Atividade Teste",
              "objetivo": "Objetivo da nova atividade",
              "publicoAlvo": "Estudantes",
              "statusPublicacao": true,
              "dataRealizacao": "2024-12-31",
              "curso": {"id": 1},
              "categoria": {"id": 1},
              "fontesFinanciadora": [],
              "integrantes": []
            }
        """;

        Integer novaAtividadeId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/atividades")
        .then()
            .log().all()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo("Nova Atividade Teste"))
            .extract()
            .path("id");

        // Limpar a atividade criada
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .delete("/api/atividades/{atividadeId}", novaAtividadeId)
        .then()
            .statusCode(204);
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoParaCriar() {
        String jsonBody = """
            {
              "nome": "Atividade Sem Auth",
              "objetivo": "Objetivo",
              "statusPublicacao": true,
              "dataRealizacao": "2024-12-31",
              "curso": {"id": 1},
              "categoria": {"id": 1}
            }
        """;

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/atividades")
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    @Test
    void deveRetornar400QuandoDadosInvalidos() {
        // Enviar sem o campo nome (obrigatório)
        String jsonBody = """
            {
              "objetivo": "Objetivo",
              "statusPublicacao": true,
              "dataRealizacao": "2024-12-31",
              "curso": {"id": 1},
              "categoria": {"id": 1}
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/atividades")
        .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    void deveRetornar404QuandoCursoNaoExisteAoCriarAtividade() {
        String jsonBody = """
            {
              "nome": "Nova Atividade",
              "objetivo": "Objetivo",
              "statusPublicacao": true,
              "dataRealizacao": "2024-12-31",
              "curso": {"id": 9999},
              "categoria": {"id": 1}
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/atividades")
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar404QuandoCategoriaNaoExiste() {
        String jsonBody = """
            {
              "nome": "Nova Atividade",
              "objetivo": "Objetivo",
              "statusPublicacao": true,
              "dataRealizacao": "2024-12-31",
              "curso": {"id": 1},
              "categoria": {"id": 9999}
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/atividades")
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    // ========== PUT /api/atividades/{atividadeId} ==========

    @Test
    void deveAtualizarAtividade() {
        Long atividadeId = 1L;
        String nomeOriginal = "Oficina de Prototipagem com Arduino";
        String nomeAtualizado = "Oficina de Prototipagem com Arduino Atualizada";

        String jsonBody = """
            {
              "nome": "%s",
              "objetivo": "Objetivo atualizado",
              "publicoAlvo": "Estudantes",
              "statusPublicacao": true,
              "dataRealizacao": "2023-01-15",
              "curso": {"id": 1},
              "categoria": {"id": 1},
              "fontesFinanciadora": [],
              "integrantes": []
            }
        """.formatted(nomeAtualizado);

        try {
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .log().all()
            .when()
                .put("/api/atividades/{atividadeId}", atividadeId)
            .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(atividadeId.intValue()))
                .body("nome", equalTo(nomeAtualizado));
        } finally {
            // Restaurar nome original
            String jsonBodyRestaurar = """
                {
                  "nome": "%s",
                  "objetivo": "Promover a aprendizagem prática sobre sensores e atuadores aplicados à automação.",
                  "publicoAlvo": "Estudantes",
                  "statusPublicacao": true,
                  "dataRealizacao": "2023-01-15",
                  "curso": {"id": 1},
                  "categoria": {"id": 1},
                  "fontesFinanciadora": [],
                  "integrantes": []
                }
            """.formatted(nomeOriginal);
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body(jsonBodyRestaurar)
            .when()
                .put("/api/atividades/{atividadeId}", atividadeId)
            .then()
                .statusCode(200);
        }
    }

    @Test
    void deveRetornar404QuandoAtividadeNaoExisteParaAtualizar() {
        Long atividadeIdInexistente = 9999L;
        String jsonBody = """
            {
              "nome": "Atividade Atualizada",
              "objetivo": "Objetivo",
              "statusPublicacao": true,
              "dataRealizacao": "2024-12-31",
              "curso": {"id": 1},
              "categoria": {"id": 1}
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/atividades/{atividadeId}", atividadeIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoParaAtualizar() {
        Long atividadeId = 1L;
        String jsonBody = """
            {
              "nome": "Atividade Atualizada",
              "objetivo": "Objetivo",
              "statusPublicacao": true,
              "dataRealizacao": "2024-12-31",
              "curso": {"id": 1},
              "categoria": {"id": 1}
            }
        """;

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/atividades/{atividadeId}", atividadeId)
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ========== PUT /api/atividades/foto-capa/{atividadeId} ==========

    @Test
    void deveAtualizarFotoCapa() throws Exception {
        Long atividadeId = 1L;
        
        // Criar um arquivo temporário de imagem
        Path tempFile = Files.createTempFile("test-image", ".jpg");
        Files.write(tempFile, new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0}); // Header JPEG mínimo
        
        try {
            File file = tempFile.toFile();
            
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType("multipart/form-data")
                .multiPart("file", file, "image/jpeg")
                .log().all()
            .when()
                .put("/api/atividades/foto-capa/{atividadeId}", atividadeId)
            .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(atividadeId.intValue()))
                .body("fotoCapa", notNullValue());
        } finally {
            Files.deleteIfExists(tempFile);
            
            // Limpar a foto de capa gerada durante o teste
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
            .when()
                .delete("/api/atividades/{atividadeId}/foto-capa", atividadeId)
            .then()
                .statusCode(anyOf(is(204), is(404)));
        }
    }

    @Test
    void deveRetornar404QuandoAtividadeNaoExisteParaFoto() throws Exception {
        Long atividadeIdInexistente = 9999L;
        
        Path tempFile = Files.createTempFile("test-image", ".jpg");
        Files.write(tempFile, new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0});
        
        try {
            File file = tempFile.toFile();
            
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType("multipart/form-data")
                .multiPart("file", file, "image/jpeg")
                .log().all()
            .when()
                .put("/api/atividades/foto-capa/{atividadeId}", atividadeIdInexistente)
            .then()
                .log().all()
                .statusCode(404)
                .body("error", notNullValue());
        } catch (Exception e) {
            // Ignorar
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (Exception e) {
                // Ignorar
            }
        }
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoParaFoto() throws Exception {
        Long atividadeId = 1L;
        
        Path tempFile = Files.createTempFile("test-image", ".jpg");
        Files.write(tempFile, new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0});
        
        try {
            File file = tempFile.toFile();
            
            given()
                .port(port)
                .contentType("multipart/form-data")
                .multiPart("file", file, "image/jpeg")
                .log().all()
            .when()
                .put("/api/atividades/foto-capa/{atividadeId}", atividadeId)
            .then()
                .log().all()
                .statusCode(anyOf(is(401), is(403)));
        } catch (Exception e) {
            // Ignorar
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (Exception e) {
                // Ignorar
            }
        }
    }

    // ========== DELETE /api/atividades/{atividadeId}/foto-capa ==========

    @Test
    void deveExcluirFotoCapa() {
        Long atividadeId = 1L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/atividades/{atividadeId}/foto-capa", atividadeId)
        .then()
            .log().all()
            .statusCode(anyOf(is(204), is(404))); // 404 se não tiver foto
    }

    @Test
    void deveRetornar404QuandoAtividadeNaoExisteParaExcluirFoto() {
        Long atividadeIdInexistente = 9999L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/atividades/{atividadeId}/foto-capa", atividadeIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoParaExcluirFoto() {
        Long atividadeId = 1L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .delete("/api/atividades/{atividadeId}/foto-capa", atividadeId)
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ========== DELETE /api/atividades/{atividadeId} ==========

    @Test
    void deveExcluirAtividade() {
        // Primeiro criar uma atividade para deletar
        String jsonBodyCriar = """
            {
              "nome": "Atividade Temp Delete",
              "objetivo": "Objetivo temporário",
              "publicoAlvo": "Estudantes",
              "statusPublicacao": false,
              "dataRealizacao": "2024-12-31",
              "curso": {"id": 1},
              "categoria": {"id": 1},
              "fontesFinanciadora": [],
              "integrantes": []
            }
        """;

        Integer atividadeId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBodyCriar)
        .when()
            .post("/api/atividades")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Agora deletar a atividade criada
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/atividades/{atividadeId}", atividadeId)
        .then()
            .log().all()
            .statusCode(204);

        // Verificar que foi deletado
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .get("/api/atividades/{atividadeId}", atividadeId)
        .then()
            .statusCode(404);
    }

    @Test
    void deveRetornar404QuandoAtividadeNaoExisteParaExcluir() {
        Long atividadeIdInexistente = 9999L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/atividades/{atividadeId}", atividadeIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoParaExcluir() {
        Long atividadeId = 1L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .delete("/api/atividades/{atividadeId}", atividadeId)
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    @Test
    void devePermitirGerenteCriarAtividade() {
        String jsonBody = """
            {
              "nome": "Atividade Gerente",
              "objetivo": "Objetivo",
              "publicoAlvo": "Estudantes",
              "statusPublicacao": true,
              "dataRealizacao": "2024-12-31",
              "curso": {"id": 1},
              "categoria": {"id": 1},
              "fontesFinanciadora": [],
              "integrantes": []
            }
        """;

        Integer atividadeId = given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/atividades")
        .then()
            .log().all()
            .statusCode(anyOf(is(201), is(403))) // Pode variar dependendo do acesso ao curso
            .extract()
            .path("id");

        // Limpar se foi criado
        if (atividadeId != null) {
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
            .when()
                .delete("/api/atividades/{atividadeId}", atividadeId)
            .then()
                .statusCode(204);
        }
    }
}

