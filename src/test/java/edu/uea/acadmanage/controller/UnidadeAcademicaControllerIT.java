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
class UnidadeAcademicaControllerIT {

    @LocalServerPort
    private int port;

    private String adminToken;
    private String gerenteToken;

    @BeforeEach
    void setUp() {
        io.restassured.RestAssured.port = port;

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

    @Test
    void deveListarUnidadesAcademicas() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .get("/api/unidades-academicas")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void deveCriarAtualizarEExcluirUnidadeAcademica() {
        String nomeOriginal = "Instituto de Ciências Aplicadas " + System.currentTimeMillis();

        Integer unidadeId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body("""
                    {
                      "nome": "%s",
                      "descricao": "Unidade criada para testes de integração."
                    }
                    """.formatted(nomeOriginal))
            .log().all()
        .when()
            .post("/api/unidades-academicas")
        .then()
            .log().all()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo(nomeOriginal))
            .extract()
            .path("id");

        String nomeAtualizado = "Instituto de Ciências Aplicadas e Tecnologia";

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body("""
                    {
                      "nome": "%s",
                      "descricao": "Descrição atualizada da unidade acadêmica."
                    }
                    """.formatted(nomeAtualizado))
            .log().all()
        .when()
            .put("/api/unidades-academicas/{id}", unidadeId)
        .then()
            .log().all()
            .statusCode(200)
            .body("nome", equalTo(nomeAtualizado));

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/unidades-academicas/{id}", unidadeId)
        .then()
            .log().all()
            .statusCode(204);
    }

    @Test
    void deveRetornar403QuandoGerenteTentaCriarUnidade() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .contentType(ContentType.JSON)
            .body("""
                    {
                      "nome": "Unidade Gerente",
                      "descricao": "Descrição"
                    }
                    """)
            .log().all()
        .when()
            .post("/api/unidades-academicas")
        .then()
            .log().all()
            .statusCode(403);
    }

    @Test
    void deveRetornar401QuandoNaoAutenticado() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body("""
                    {
                      "nome": "Unidade Sem Auth",
                      "descricao": "Descrição"
                    }
                    """)
            .log().all()
        .when()
            .post("/api/unidades-academicas")
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }
}

