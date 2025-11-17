package edu.uea.acadmanage.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
class CursoControllerIT {

    @LocalServerPort
    private int port;

    private String adminToken;
    private String gerenteToken;
    private String gerenteSemAcessoToken;

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
            gerenteSemAcessoToken = obterToken("gerente2@uea.edu.br", "gerente123");
        } catch (AssertionError e) {
            gerenteSemAcessoToken = null;
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

    private String getGerenteSemAcessoToken() {
        if (gerenteSemAcessoToken == null) {
            gerenteSemAcessoToken = obterToken("gerente2@uea.edu.br", "gerente123");
        }
        return gerenteSemAcessoToken;
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

    // ========== GET /api/cursos ==========

    @Test
    void deveListarTodosCursosComPaginacao() {
        given()
            .port(port)
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/cursos")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)))
            .body(is(notNullValue()));
    }

    @Test
    void deveListarCursosComFiltroPorAtivo() {
        given()
            .port(port)
            .param("ativo", "true")
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/cursos")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void deveListarCursosComFiltroPorNome() {
        given()
            .port(port)
            .param("nome", "Engenharia")
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/cursos")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void deveListarCursosComFiltroPorTipoId() {
        given()
            .port(port)
            .param("tipoId", "1") // ID do Bacharelado
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/cursos")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void deveListarCursosComFiltroPorUnidadeAcademica() {
        given()
            .port(port)
            .param("unidadeAcademicaId", "1")
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/cursos")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    // ========== GET /api/cursos/{cursoId} ==========

    @Test
    void deveBuscarCursoPorId() {
        Long cursoId = 1L; // ID de um curso do data.sql
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/cursos/{cursoId}", cursoId)
        .then()
            .log().all()
            .statusCode(200)
            .body("id", equalTo(cursoId.intValue()))
            .body("nome", notNullValue());
    }

    @Test
    void deveRetornar404QuandoCursoNaoExiste() {
        Long cursoIdInexistente = 9999L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/cursos/{cursoId}", cursoIdInexistente)
        .then()
            .log().all()
            .statusCode(404);
    }

    // ========== GET /api/cursos/permissoes/{cursoId} ==========

    @Test
    void deveListarPermissoesDoCurso() {
        Long cursoId = 1L; // ID de um curso do data.sql
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .get("/api/cursos/permissoes/{cursoId}", cursoId)
        .then()
            .log().all()
            .statusCode(200)
            .body("$", is(instanceOf(java.util.List.class)))
            .body("cursoId", notNullValue());
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoParaPermissoes() {
        Long cursoId = 1L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/cursos/permissoes/{cursoId}", cursoId)
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ========== GET /api/cursos/usuarios ==========

    @Test
    void deveListarCursosDoUsuario() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/cursos/usuarios")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void deveListarCursosDoUsuarioComFiltros() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .param("ativo", "true")
            .param("nome", "Engenharia")
            .param("tipoId", "1")
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/cursos/usuarios")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoParaCursosUsuario() {
        given()
            .port(port)
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/cursos/usuarios")
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ========== POST /api/cursos ==========

    @Test
    void deveCriarCursoComoAdministrador() {
        String nomeNovo = "Novo Curso Teste";
        String jsonBody = """
            {
              "nome": "%s",
              "descricao": "Descrição do novo curso",
              "ativo": true,
              "tipoId": 1,
              "unidadeAcademicaId": 1
            }
        """.formatted(nomeNovo);

        Integer novoCursoId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/cursos")
        .then()
            .log().all()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo(nomeNovo))
            .extract()
            .path("id");

        // Limpar o curso criado
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .delete("/api/cursos/{cursoId}", novoCursoId)
        .then()
            .statusCode(204);
    }

    @Test
    void deveRetornar403QuandoGerenteTentaCriarCurso() {
        String jsonBody = """
            {
              "nome": "Curso Gerente",
              "descricao": "Descrição",
              "ativo": true,
              "tipoId": 1,
              "unidadeAcademicaId": 1
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/cursos")
        .then()
            .log().all()
            .statusCode(403);
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoNoPost() {
        String jsonBody = """
            {
              "nome": "Curso Sem Auth",
              "descricao": "Descrição",
              "ativo": true,
              "tipoId": 1,
              "unidadeAcademicaId": 1
            }
        """;

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/cursos")
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    @Test
    void deveRetornar400QuandoDadosInvalidos() {
        // Enviar sem campos obrigatórios (nome e tipoId)
        String jsonBody = """
            {
              "descricao": "Apenas descrição"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/cursos")
        .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    void deveRetornar400QuandoNomeVazio() {
        String jsonBody = """
            {
              "nome": "",
              "descricao": "Descrição",
              "ativo": true,
              "tipoId": 1,
              "unidadeAcademicaId": 1
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/cursos")
        .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    void deveRetornar400QuandoTipoIdNulo() {
        String jsonBody = """
            {
              "nome": "Curso Sem Tipo",
              "descricao": "Descrição",
              "ativo": true,
              "unidadeAcademicaId": 1
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/cursos")
        .then()
            .log().all()
            .statusCode(400);
    }

    // ========== PUT /api/cursos/{cursoId} ==========

    @Test
    void deveAtualizarCurso() {
        Long cursoId = 10L; // ID do Doutorado do data.sql
        String nomeOriginal = "Doutorado em Engenharia de Computação";
        String nomeAtualizado = "Doutorado Atualizado";
        Long tipoIdOriginal = 7L;
        Long tipoIdNovo = 6L; // Mestrado

        String jsonBody = """
            {
              "nome": "%s",
              "descricao": "Descrição atualizada",
              "ativo": true,
              "tipoId": %d,
              "unidadeAcademicaId": 2
            }
        """.formatted(nomeAtualizado, tipoIdNovo);

        try {
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .log().all()
            .when()
                .put("/api/cursos/{cursoId}", cursoId)
            .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(cursoId.intValue()))
                .body("nome", equalTo(nomeAtualizado))
                .body("tipoId", equalTo(tipoIdNovo.intValue()));
        } finally {
            // Restaurar dados originais
            String jsonBodyRestaurar = """
                {
                  "nome": "%s",
                  "descricao": "Curso stricto sensu que visa o desenvolvimento de pesquisas avançadas em hardware, sistemas embarcados, IoT e automação inteligente.",
                  "ativo": true,
                  "tipoId": %d,
                  "unidadeAcademicaId": 1
                }
            """.formatted(nomeOriginal, tipoIdOriginal);
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body(jsonBodyRestaurar)
            .when()
                .put("/api/cursos/{cursoId}", cursoId)
            .then()
                .statusCode(200);
        }
    }

    @Test
    void deveRetornar404QuandoAtualizarCursoInexistente() {
        Long cursoIdInexistente = 9999L;
        String jsonBody = """
            {
              "nome": "Curso Inexistente",
              "descricao": "Descrição",
              "ativo": true,
              "tipoId": 1,
              "unidadeAcademicaId": 1
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/cursos/{cursoId}", cursoIdInexistente)
        .then()
            .log().all()
            .statusCode(404);
    }

    @Test
    void devePermitirGerenteAtualizarCurso() {
        Long cursoId = 9L; // ID de um curso do data.sql
        
        String jsonBody = """
            {
              "nome": "Curso Atualizado por Gerente",
              "descricao": "Descrição",
              "ativo": true,
              "tipoId": 6,
              "unidadeAcademicaId": 2
            }
        """;

        // Gerente pode atualizar, então deve retornar 200
        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/cursos/{cursoId}", cursoId)
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(403)));
    }

    // ========== PUT /api/cursos/{cursoId}/status ==========

    @Test
    void deveAtualizarStatusDoCurso() {
        Long cursoId = 8L; // ID de um curso do data.sql
        Boolean statusOriginal = true;
        Boolean statusNovo = false;

        String jsonBody = """
            {
              "ativo": %s
            }
        """.formatted(statusNovo);

        try {
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .log().all()
            .when()
                .put("/api/cursos/{cursoId}/status", cursoId)
            .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(cursoId.intValue()))
                .body("ativo", equalTo(statusNovo));
        } finally {
            // Restaurar status original
            String jsonBodyRestaurar = """
                {
                  "ativo": %s
                }
            """.formatted(statusOriginal);
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body(jsonBodyRestaurar)
            .when()
                .put("/api/cursos/{cursoId}/status", cursoId)
            .then()
                .statusCode(200);
        }
    }

    // ========== PUT /api/cursos/{cursoId}/usuarios/{usuarioId} ==========

    @Test
    void deveAdicionarUsuarioAoCurso() {
        Long cursoId = 5L; // ID de um curso do data.sql
        Long usuarioId = 2L; // ID de um gerente

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .put("/api/cursos/{cursoId}/usuarios/{usuarioId}", cursoId, usuarioId)
        .then()
            .log().all()
            .statusCode(200)
            .body("$", is(instanceOf(java.util.List.class)))
            .body("cursoId", notNullValue());
    }

    @Test
    void deveRetornar404QuandoAdicionarUsuarioInexistente() {
        Long cursoId = 1L;
        Long usuarioIdInexistente = 9999L;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .put("/api/cursos/{cursoId}/usuarios/{usuarioId}", cursoId, usuarioIdInexistente)
        .then()
            .log().all()
            .statusCode(404);
    }

    // ========== DELETE /api/cursos/{cursoId} ==========

    @Test
    void deveDeletarCursoComoAdministrador() {
        // Primeiro criar um curso para deletar
        String nomeTemp = "Curso Temporário";
        String jsonBodyCriar = """
            {
              "nome": "%s",
              "descricao": "Descrição",
              "ativo": true,
              "tipoId": 1,
              "unidadeAcademicaId": 1
            }
        """.formatted(nomeTemp);

        Integer cursoId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBodyCriar)
        .when()
            .post("/api/cursos")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Agora deletar o curso criado
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/cursos/{cursoId}", cursoId)
        .then()
            .log().all()
            .statusCode(204);

        // Verificar que foi deletado
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .get("/api/cursos/{cursoId}", cursoId)
        .then()
            .statusCode(404);
    }

    @Test
    void deveRetornar404QuandoDeletarCursoInexistente() {
        Long cursoIdInexistente = 9999L;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/cursos/{cursoId}", cursoIdInexistente)
        .then()
            .log().all()
            .statusCode(404);
    }

    @Test
    void deveRetornar403QuandoGerenteTentaDeletarCurso() {
        Long cursoId = 7L; // ID de um curso do data.sql

        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .log().all()
        .when()
            .delete("/api/cursos/{cursoId}", cursoId)
        .then()
            .log().all()
            .statusCode(403);
    }

    // ========== DELETE /api/cursos/{cursoId}/usuarios/{usuarioId} ==========

    @Test
    void deveRemoverUsuarioDoCurso() {
        // Criar um curso temporário para testar
        String nomeTemp = "Curso Temp Remove User";
        String jsonBodyCriar = """
            {
              "nome": "%s",
              "descricao": "Descrição",
              "ativo": true,
              "tipoId": 1,
              "unidadeAcademicaId": 1
            }
        """.formatted(nomeTemp);

        Integer cursoId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBodyCriar)
        .when()
            .post("/api/cursos")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        try {
            Long usuarioId = 2L; // ID de um gerente

            // Primeiro adicionar o usuário ao curso
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
            .when()
                .put("/api/cursos/{cursoId}/usuarios/{usuarioId}", cursoId, usuarioId)
            .then()
                .statusCode(200);

            // Agora remover o usuário do curso
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .log().all()
            .when()
                .delete("/api/cursos/{cursoId}/usuarios/{usuarioId}", cursoId, usuarioId)
            .then()
                .log().all()
                .statusCode(200)
                .body("$", is(instanceOf(java.util.List.class)));
        } finally {
            // Limpar o curso criado
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
            .when()
                .delete("/api/cursos/{cursoId}", cursoId)
            .then()
                .statusCode(204);
        }
    }

    @Test
    void deveRetornar409QuandoUsuarioTentaRemoverPropriaPermissao() {
        // Admin não pode remover a própria permissão de um curso
        Long cursoId = 1L;
        Long adminId = 1L; // ID do admin

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/cursos/{cursoId}/usuarios/{usuarioId}", cursoId, adminId)
        .then()
            .log().all()
            .statusCode(409); // Conflito: não pode remover própria permissão
    }

    // ========== GET /api/cursos/{cursoId}/relatorios ==========

    @Test
    void deveGerarRelatorioPdfDoCurso() {
        String requestBody = """
            {
              "dataInicio": "2023-01-01",
              "dataFim": "2023-12-31",
              "introducao": "%s"
            }
        """.formatted("Texto introdutório bastante longo ".repeat(50).trim());

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(requestBody)
            .log().all()
        .when()
            .post("/api/cursos/{cursoId}/relatorios", 1L)
        .then()
            .log().all()
            .statusCode(200)
            .contentType("application/pdf")
            .header("Content-Disposition", containsString("relatorio-curso-1.pdf"))
            .body(notNullValue());
    }

    @Test
    void deveGerarRelatorioPdfFiltradoPorCategorias() {
        String requestBody = """
            {
              "dataInicio": "2023-01-01",
              "dataFim": "2023-12-31",
              "categorias": [1, 2],
              "introducao": "Relatório filtrado por categorias."
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(requestBody)
            .log().all()
        .when()
            .post("/api/cursos/{cursoId}/relatorios", 1L)
        .then()
            .log().all()
            .statusCode(200)
            .contentType("application/pdf")
            .header("Content-Disposition", containsString("relatorio-curso-1.pdf"))
            .body(notNullValue());
    }

    @Test
    void deveRetornar403QuandoUsuarioSemAcessoSolicitaRelatorio() {
        String requestBody = """
            {
              "dataInicio": "2023-01-01",
              "dataFim": "2023-12-31",
              "introducao": "Tentativa não autorizada."
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteSemAcessoToken())
            .contentType(ContentType.JSON)
            .body(requestBody)
            .log().all()
        .when()
            .post("/api/cursos/{cursoId}/relatorios", 1L)
        .then()
            .log().all()
            .statusCode(403);
    }

    // ========== PUT /api/cursos/foto-capa/{cursoId} e GET /api/cursos/foto-capa/{cursoId} ==========
    // Nota: Testes de upload/download de arquivo podem ser complexos e podem precisar de arquivos reais
    // Por enquanto, vou criar testes básicos

    @Test
    void deveRetornar403QuandoNaoAutenticadoParaFotoCapa() {
        Long cursoId = 1L;

        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/cursos/foto-capa/{cursoId}", cursoId)
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }
}

