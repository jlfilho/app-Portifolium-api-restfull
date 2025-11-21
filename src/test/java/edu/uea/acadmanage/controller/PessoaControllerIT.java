package edu.uea.acadmanage.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Random;

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

    /**
     * Gera um CPF válido para testes.
     * O CPF gerado passa na validação algorítmica do dígito verificador.
     */
    private String gerarCpfValido() {
        Random random = new Random();
        int[] cpf = new int[11];

        // Gera os 9 primeiros dígitos aleatórios (evitando todos iguais)
        do {
            for (int i = 0; i < 9; i++) {
                cpf[i] = random.nextInt(10);
            }
        } while (todosIguais(cpf, 9));

        // Calcula o primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += cpf[i] * (10 - i);
        }
        int resto = soma % 11;
        cpf[9] = (resto < 2) ? 0 : 11 - resto;

        // Calcula o segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += cpf[i] * (11 - i);
        }
        resto = soma % 11;
        cpf[10] = (resto < 2) ? 0 : 11 - resto;

        // Retorna apenas dígitos (sem formatação)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            sb.append(cpf[i]);
        }
        return sb.toString();
    }

    private boolean todosIguais(int[] array, int tamanho) {
        for (int i = 1; i < tamanho; i++) {
            if (array[i] != array[0]) {
                return false;
            }
        }
        return true;
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
        String cpf = gerarCpfValido();

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
        String cpf = gerarCpfValido();

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
        String cpf = gerarCpfValido();

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
        String cpf1 = gerarCpfValido();
        String cpf2 = gerarCpfValido();
        String cpf3 = "12345678909"; // CPF válido conhecido
        String csv = """
                nome,cpf
                Pessoa CSV 1,%s
                Pessoa CSV 2,%s
                João Silva,%s
                """.formatted(cpf1, cpf2, cpf3);

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
        String cpf = gerarCpfValido();
        given()
                .port(port)
                .header("Authorization", "Bearer " + getGerenteToken())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nome": "Pessoa Gerente",
                          "cpf": "%s"
                        }
                        """.formatted(cpf))
        .when()
                .post("/api/pessoas")
        .then()
                .statusCode(403);
    }
}

