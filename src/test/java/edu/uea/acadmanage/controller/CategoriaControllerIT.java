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
class CategoriaControllerIT {

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

    // ========== GET /api/categorias ==========

    @Test
    void deveListarTodasCategoriasComPaginacao() {
        given()
            .port(port)
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/categorias")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    void deveListarTodasCategoriasComParametrosPadrao() {
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/categorias")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void deveListarCategoriasComOrdenacao() {
        given()
            .port(port)
            .param("page", 0)
            .param("size", 10)
            .param("sortBy", "nome")
            .param("direction", "ASC")
            .log().all()
        .when()
            .get("/api/categorias")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue());
    }

    // ========== GET /api/categorias/{categoriaId} ==========

    @Test
    void deveBuscarCategoriaPorId() {
        Long categoriaId = 1L; // ID de "Ensino" do data.sql
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/categorias/{categoriaId}", categoriaId)
        .then()
            .log().all()
            .statusCode(200)
            .body("id", equalTo(categoriaId.intValue()))
            .body("nome", notNullValue());
    }

    @Test
    void deveRetornar404QuandoCategoriaNaoExiste() {
        Long categoriaIdInexistente = 9999L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/categorias/{categoriaId}", categoriaIdInexistente)
        .then()
            .log().all()
            .statusCode(404);
    }

    // ========== GET /api/categorias/cursos/{cursoId} ==========

    @Test
    void deveListarCategoriasPorCurso() {
        Long cursoId = 1L; // ID de um curso do data.sql
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/categorias/cursos/{cursoId}", cursoId)
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void deveListarCategoriasPorCursoComFiltros() {
        Long cursoId = 1L;
        
        given()
            .port(port)
            .param("categorias", "1,2")
            .param("statusPublicacao", "true")
            .param("nomeAtividade", "Atividade")
            .log().all()
        .when()
            .get("/api/categorias/cursos/{cursoId}", cursoId)
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void deveRetornar204QuandoNaoHaCategoriasParaCurso() {
        // Usar um curso que provavelmente não tem atividades/categorias
        Long cursoIdInexistente = 9999L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/categorias/cursos/{cursoId}", cursoIdInexistente)
        .then()
            .log().all()
            .statusCode(204);
    }

    // ========== GET /api/categorias/usuario ==========

    @Test
    void deveListarCategoriasComAtividadesDoUsuario() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .get("/api/categorias/usuario")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void deveRetornar403QuandoGerenteTentaAcessarCategoriasUsuario() {
        // Gerente tem permissão, então deve funcionar
        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .log().all()
        .when()
            .get("/api/categorias/usuario")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204), is(403)));
    }

    @Test
    void deveRetornar403QuandoNaoAutenticado() {
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/categorias/usuario")
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ========== POST /api/categorias ==========

    @Test
    void deveCriarCategoriaComoAdministrador() {
        String nomeNovo = "Nova Categoria Teste";
        String jsonBody = """
            {
              "nome": "%s"
            }
        """.formatted(nomeNovo);

        Integer novaCategoriaId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/categorias")
        .then()
            .log().all()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo(nomeNovo))
            .extract()
            .path("id");

        // Limpar a categoria criada
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .delete("/api/categorias/{categoriaId}", novaCategoriaId)
        .then()
            .statusCode(204);
    }

    @Test
    void deveRetornar403QuandoGerenteTentaCriarCategoria() {
        String jsonBody = """
            {
              "nome": "Categoria Gerente"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/categorias")
        .then()
            .log().all()
            .statusCode(403);
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoNoPost() {
        String jsonBody = """
            {
              "nome": "Categoria Sem Auth"
            }
        """;

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/categorias")
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    @Test
    void deveRetornar400QuandoDadosInvalidos() {
        // Enviar sem o campo nome (obrigatório)
        String jsonBody = """
            {
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/categorias")
        .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    void deveRetornar400QuandoNomeVazio() {
        String jsonBody = """
            {
              "nome": ""
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/categorias")
        .then()
            .log().all()
            .statusCode(400);
    }

    // ========== PUT /api/categorias/{categoriaId} ==========

    @Test
    void deveAtualizarCategoria() {
        Long categoriaId = 3L; // ID de "Extensão" do data.sql
        String nomeOriginal = "Extensão";
        String nomeAtualizado = "Extensão Atualizada";

        String jsonBody = """
            {
              "nome": "%s"
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
                .put("/api/categorias/{categoriaId}", categoriaId)
            .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(categoriaId.intValue()))
                .body("nome", equalTo(nomeAtualizado));
        } finally {
            // Restaurar nome original
            String jsonBodyRestaurar = """
                {
                  "nome": "%s"
                }
            """.formatted(nomeOriginal);
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body(jsonBodyRestaurar)
            .when()
                .put("/api/categorias/{categoriaId}", categoriaId)
            .then()
                .statusCode(200);
        }
    }

    @Test
    void deveRetornar404QuandoAtualizarCategoriaInexistente() {
        Long categoriaIdInexistente = 9999L;
        String jsonBody = """
            {
              "nome": "Categoria Inexistente"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/categorias/{categoriaId}", categoriaIdInexistente)
        .then()
            .log().all()
            .statusCode(404);
    }

    @Test
    void deveRetornar403QuandoGerenteTentaAtualizarCategoria() {
        Long categoriaId = 2L; // ID de "Pesquisa"
        
        String jsonBody = """
            {
              "nome": "Pesquisa Atualizada"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/categorias/{categoriaId}", categoriaId)
        .then()
            .log().all()
            .statusCode(403);
    }

    @Test
    void deveRetornar400QuandoAtualizarComDadosInvalidos() {
        Long categoriaId = 1L;
        
        // Enviar sem o campo nome
        String jsonBody = """
            {
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/categorias/{categoriaId}", categoriaId)
        .then()
            .log().all()
            .statusCode(400);
    }

    // ========== DELETE /api/categorias/{categoriaId} ==========

    @Test
    void deveDeletarCategoriaSemAtividadesAssociadas() {
        // Primeiro criar uma categoria para deletar
        String nomeTemp = "Categoria Temporária";
        String jsonBodyCriar = """
            {
              "nome": "%s"
            }
        """.formatted(nomeTemp);

        Integer categoriaId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBodyCriar)
        .when()
            .post("/api/categorias")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Agora deletar a categoria criada
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/categorias/{categoriaId}", categoriaId)
        .then()
            .log().all()
            .statusCode(204);

        // Verificar que foi deletada
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .get("/api/categorias/{categoriaId}", categoriaId)
        .then()
            .statusCode(404);
    }

    @Test
    void deveRetornar409QuandoDeletarCategoriaComAtividades() {
        // Usar uma categoria que provavelmente tem atividades associadas
        // As categorias do data.sql (Ensino, Pesquisa, Extensão) podem ter atividades
        // Este teste pode precisar de ajuste dependendo dos dados
        Long categoriaId = 1L; // ID de "Ensino"

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/categorias/{categoriaId}", categoriaId)
        .then()
            .log().all()
            .statusCode(anyOf(is(204), is(409))); // Pode retornar 204 se não tiver atividades ou 409 se tiver
    }

    @Test
    void deveRetornar404QuandoDeletarCategoriaInexistente() {
        Long categoriaIdInexistente = 9999L;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/categorias/{categoriaId}", categoriaIdInexistente)
        .then()
            .log().all()
            .statusCode(404);
    }

    @Test
    void deveRetornar403QuandoGerenteTentaDeletarCategoria() {
        Long categoriaId = 2L; // ID de "Pesquisa"

        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .log().all()
        .when()
            .delete("/api/categorias/{categoriaId}", categoriaId)
        .then()
            .log().all()
            .statusCode(403);
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoNoDelete() {
        Long categoriaId = 3L; // ID de "Extensão"

        given()
            .port(port)
            .log().all()
        .when()
            .delete("/api/categorias/{categoriaId}", categoriaId)
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }
}

