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
class TipoCursoControllerIT {

    @LocalServerPort
    private int port;

    private String adminToken;
    private String gerenteToken;

    @BeforeEach
    void setUp() {
        // Configurar a porta base do REST Assured
        io.restassured.RestAssured.port = port;
        
        // Obter tokens de autenticação
        // Se falhar, será obtido lazy quando necessário
        try {
            adminToken = obterToken("admin@uea.edu.br", "admin123");
        } catch (AssertionError e) {
            adminToken = null; // Será obtido quando necessário
        }
        
        try {
            gerenteToken = obterToken("gerente1@uea.edu.br", "gerente123");
        } catch (AssertionError e) {
            gerenteToken = null; // Será obtido quando necessário
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

    @Test
    void deveListarTiposCursoComPaginacao() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/tipos-curso")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    void deveListarTiposCursoComFiltroPorNome() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .param("nome", "Bacharelado")
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/tipos-curso")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void deveRetornar204QuandoNaoHaResultados() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .param("nome", "TipoInexistente12345")
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/tipos-curso")
        .then()
            .log().all()
            .statusCode(204);
    }

    @Test
    void deveBuscarTipoCursoPorId() {
        Long tipoCursoId = 1L; // ID do Bacharelado do data.sql
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .get("/api/tipos-curso/{id}", tipoCursoId)
        .then()
            .log().all()
            .statusCode(200)
            .body("id", equalTo(tipoCursoId.intValue()))
            .body("nome", notNullValue());
    }

    @Test
    void deveRetornar404QuandoTipoCursoNaoExiste() {
        Long tipoCursoIdInexistente = 9999L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .get("/api/tipos-curso/{id}", tipoCursoIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveCriarTipoCursoComoAdministrador() {
        String nomeNovo = "Pós-Graduação Teste " + System.currentTimeMillis();
        String jsonBody = """
            {
              "nome": "%s"
            }
        """.formatted(nomeNovo);

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/tipos-curso")
        .then()
            .log().all()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo(nomeNovo));
    }

    @Test
    void deveRetornar403QuandoGerenteTentaCriarTipoCurso() {
        String jsonBody = """
            {
              "nome": "Tipo Teste Gerente"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/tipos-curso")
        .then()
            .log().all()
            .statusCode(403);
    }

    @Test
    void deveRetornar409QuandoNomeDuplicado() {
        String nomeDuplicado = "Bacharelado"; // Nome que já existe no data.sql
        String jsonBody = """
            {
              "nome": "%s"
            }
        """.formatted(nomeDuplicado);

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/tipos-curso")
        .then()
            .log().all()
            .statusCode(409)
            .body("error", notNullValue())
            .body("status", equalTo("CONFLICT"));
    }

    @Test
    void deveAtualizarTipoCurso() {
        // Primeiro criar um tipo de curso para atualizar
        String nomeOriginal = "Tipo Temporário " + System.currentTimeMillis();
        String jsonBodyCriar = """
            {
              "nome": "%s"
            }
        """.formatted(nomeOriginal);

        Integer tipoCursoId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBodyCriar)
        .when()
            .post("/api/tipos-curso")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Agora atualizar
        String nomeAtualizado = "Tipo Atualizado";
        String jsonBodyAtualizar = """
            {
              "nome": "%s"
            }
        """.formatted(nomeAtualizado);

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBodyAtualizar)
            .log().all()
        .when()
            .put("/api/tipos-curso/{id}", tipoCursoId)
        .then()
            .log().all()
            .statusCode(200)
            .body("id", equalTo(tipoCursoId))
            .body("nome", equalTo(nomeAtualizado));

        // Limpar - deletar o tipo criado
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .delete("/api/tipos-curso/{id}", tipoCursoId)
        .then()
            .statusCode(204);
    }

    @Test
    void deveRetornar409QuandoAtualizarComNomeDuplicado() {
        // Criar um tipo para atualizar
        String nomeTemporario = "Tipo Temp Atualização " + System.currentTimeMillis();
        Integer tipoCursoId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body("""
                {
                  "nome": "%s"
                }
            """.formatted(nomeTemporario))
        .when()
            .post("/api/tipos-curso")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Tentar atualizar com nome que já existe
        String jsonBody = """
            {
              "nome": "Bacharelado"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/tipos-curso/{id}", tipoCursoId)
        .then()
            .log().all()
            .statusCode(409)
            .body("error", notNullValue());

        // Limpar
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .delete("/api/tipos-curso/{id}", tipoCursoId)
        .then()
            .statusCode(204);
    }

    @Test
    void deveRetornar404QuandoAtualizarTipoCursoInexistente() {
        Long tipoCursoIdInexistente = 9999L;
        String jsonBody = """
            {
              "nome": "Nome Qualquer"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/tipos-curso/{id}", tipoCursoIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveDeletarTipoCursoSemCursosAssociados() {
        // Primeiro criar um tipo de curso sem cursos associados
        String nomeTemporario = "Tipo Para Deletar " + System.currentTimeMillis();
        Integer tipoCursoId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body("""
                {
                  "nome": "%s"
                }
            """.formatted(nomeTemporario))
        .when()
            .post("/api/tipos-curso")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Deletar o tipo criado
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/tipos-curso/{id}", tipoCursoId)
        .then()
            .log().all()
            .statusCode(204);

        // Verificar que foi deletado
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .get("/api/tipos-curso/{id}", tipoCursoId)
        .then()
            .statusCode(404);
    }

    @Test
    void deveRetornar409QuandoDeletarTipoCursoEmUso() {
        Long tipoCursoIdEmUso = 1L; // ID do Bacharelado que tem cursos associados no data.sql
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/tipos-curso/{id}", tipoCursoIdEmUso)
        .then()
            .log().all()
            .statusCode(409)
            .body("mensagem", notNullValue())
            .body("tipoCursoId", equalTo(String.valueOf(tipoCursoIdEmUso)));
    }

    @Test
    void deveRetornar404QuandoDeletarTipoCursoInexistente() {
        Long tipoCursoIdInexistente = 9999L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/tipos-curso/{id}", tipoCursoIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar403QuandoGerenteTentaDeletarTipoCurso() {
        Long tipoCursoId = 2L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .log().all()
        .when()
            .delete("/api/tipos-curso/{id}", tipoCursoId)
        .then()
            .log().all()
            .statusCode(403);
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
            .post("/api/tipos-curso")
        .then()
            .log().all()
            .statusCode(400);
    }
}

