package edu.uea.acadmanage.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.nio.charset.StandardCharsets;

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
class PessoaControllerIT {

    @LocalServerPort
    private int port;

    private String adminToken;
    private String gerenteToken;

    @BeforeEach
    void setUp() {
        io.restassured.RestAssured.port = port;
        adminToken = obterToken("admin@uea.edu.br", "admin123");
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
    void deveListarPessoas() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
        .when()
                .get("/api/pessoas")
        .then()
                .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void deveCriarPessoaComoAdministrador() {
        String cpf = "99887766550";

        given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Pessoa Teste",
                          "cpf": "%s"
                        }
                        """.formatted(cpf))
        .when()
                .post("/api/pessoas")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("nome", equalTo("Pessoa Teste"))
                .body("cpf", equalTo(cpf));
    }

    @Test
    void deveRetornar409QuandoCpfDuplicado() {
        String cpf = "99887766551";

        // Cadastro inicial
        given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Pessoa Duplicada",
                          "cpf": "%s"
                        }
                        """.formatted(cpf))
        .when()
                .post("/api/pessoas")
        .then()
                .statusCode(201);

        // Tentativa duplicada
        given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Outra Pessoa",
                          "cpf": "%s"
                        }
                        """.formatted(cpf))
        .when()
                .post("/api/pessoas")
        .then()
                .statusCode(409)
                .body("error", containsString("Pessoa"));
    }

    @Test
    void deveAtualizarPessoa() {
        String cpf = "99887766552";

        Integer pessoaId = given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Pessoa Atualizar",
                          "cpf": "%s"
                        }
                        """.formatted(cpf))
        .when()
                .post("/api/pessoas")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Pessoa Atualizada",
                          "cpf": "%s"
                        }
                        """.formatted(cpf))
        .when()
                .put("/api/pessoas/{id}", pessoaId)
        .then()
                .statusCode(200)
                .body("nome", equalTo("Pessoa Atualizada"));
    }

    @Test
    void devePermitirImportarCsv() {
        String csv = """
                nome,cpf
                Pessoa CSV 1,99887766553
                Pessoa CSV 2,99887766554
                João Silva,12345678901
                """;

        given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .multiPart("file", "pessoas.csv", csv.getBytes(StandardCharsets.UTF_8), "text/csv")
        .when()
                .post("/api/pessoas/import")
        .then()
                .statusCode(201)
                .body("totalProcessados", equalTo(3))
                .body("totalCadastrados", equalTo(2))
                .body("duplicados", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void deveNegarCriacaoParaGerente() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + getGerenteToken())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Pessoa Gerente",
                          "cpf": "99887766555"
                        }
                        """)
        .when()
                .post("/api/pessoas")
        .then()
                .statusCode(403);
    }
}

