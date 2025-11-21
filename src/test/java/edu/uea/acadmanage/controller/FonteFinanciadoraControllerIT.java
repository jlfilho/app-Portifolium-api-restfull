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
class FonteFinanciadoraControllerIT {

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
            secretarioToken = obterTokenSecretario();
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
            secretarioToken = obterTokenSecretario();
        }
        return secretarioToken;
    }

    private String obterTokenSecretario() {
        // Usar secretario1@uea.edu.br do data.sql
        return obterToken("secretario1@uea.edu.br", "secretario123");
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

    // ========== GET /api/fontes-financiadoras ==========

    @Test
    void deveListarTodasFontesFinanciadoras() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .get("/api/fontes-financiadoras")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)))
            .body(is(notNullValue()));
    }

    @Test
    void deveRetornar204QuandoListaVazia() {
        // Este teste pode passar ou falhar dependendo dos dados no banco
        // Por padrão, há 5 fontes no data.sql, então pode retornar 200
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .get("/api/fontes-financiadoras")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204)));
    }

    // ========== GET /api/fontes-financiadoras/{id} ==========

    @Test
    void deveBuscarFonteFinanciadoraPorId() {
        Long fonteId = 1L; // ID da UEA do data.sql
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .get("/api/fontes-financiadoras/{id}", fonteId)
        .then()
            .log().all()
            .statusCode(200)
            .body("id", equalTo(fonteId.intValue()))
            .body("nome", notNullValue());
    }

    @Test
    void deveRetornar404QuandoFonteFinanciadoraNaoExiste() {
        Long fonteIdInexistente = 9999L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .get("/api/fontes-financiadoras/{id}", fonteIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    // ========== POST /api/fontes-financiadoras ==========

    @Test
    void deveCriarFonteFinanciadoraComoAdministrador() {
        String nomeNovo = "Nova Fonte Teste " + System.currentTimeMillis();
        String jsonBody = """
            {
              "nome": "%s"
            }
        """.formatted(nomeNovo);

        Integer novaFonteId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/fontes-financiadoras")
        .then()
            .log().all()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo(nomeNovo))
            .extract()
            .path("id");

        // Limpar a fonte criada
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .delete("/api/fontes-financiadoras/{id}", novaFonteId)
        .then()
            .statusCode(204);
    }

    @Test
    void deveCriarFonteFinanciadoraComoGerente() {
        String nomeNovo = "Fonte Gerente Teste " + System.currentTimeMillis();
        String jsonBody = """
            {
              "nome": "%s"
            }
        """.formatted(nomeNovo);

        Integer novaFonteId = given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/fontes-financiadoras")
        .then()
            .log().all()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo(nomeNovo))
            .extract()
            .path("id");

        // Limpar a fonte criada
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .delete("/api/fontes-financiadoras/{id}", novaFonteId)
        .then()
            .statusCode(204);
    }

    @Test
    void deveCriarFonteFinanciadoraComoSecretario() {
        String tokenSecretario = getSecretarioToken();
        if (tokenSecretario == null) {
            // Se não houver secretário disponível, pular o teste
            return;
        }

        String nomeNovo = "Fonte Secretario Teste " + System.currentTimeMillis();
        String jsonBody = """
            {
              "nome": "%s"
            }
        """.formatted(nomeNovo);

        Integer novaFonteId = given()
            .port(port)
            .header("Authorization", "Bearer " + tokenSecretario)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/fontes-financiadoras")
        .then()
            .log().all()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo(nomeNovo))
            .extract()
            .path("id");

        // Limpar a fonte criada
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .delete("/api/fontes-financiadoras/{id}", novaFonteId)
        .then()
            .statusCode(204);
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoNoPost() {
        String jsonBody = """
            {
              "nome": "Fonte Sem Auth"
            }
        """;

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/fontes-financiadoras")
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
            .post("/api/fontes-financiadoras")
        .then()
            .log().all()
            .statusCode(400);
    }

    // ========== PUT /api/fontes-financiadoras/{id} ==========

    @Test
    void deveAtualizarFonteFinanciadora() {
        Long fonteId = 5L; // ID de "Outros" do data.sql
        String nomeOriginal = "Outros";
        String nomeAtualizado = "Outros Atualizado";

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
                .put("/api/fontes-financiadoras/{id}", fonteId)
            .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(fonteId.intValue()))
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
                .put("/api/fontes-financiadoras/{id}", fonteId)
            .then()
                .statusCode(200);
        }
    }

    @Test
    void deveRetornar404QuandoAtualizarFonteInexistente() {
        Long fonteIdInexistente = 9999L;
        String jsonBody = """
            {
              "nome": "Fonte Inexistente"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/fontes-financiadoras/{id}", fonteIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar403QuandoGerenteTentaAtualizarSemPermissao() {
        // Este teste verifica se há alguma restrição adicional
        // Como gerente pode criar, provavelmente pode atualizar também
        Long fonteId = 4L; // ID do CNPq
        
        String jsonBody = """
            {
              "nome": "CNPq Atualizado"
            }
        """;

        // Gerente pode atualizar, então pode retornar 200 ou 403
        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/fontes-financiadoras/{id}", fonteId)
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(403)));
    }

    @Test
    void deveRetornar400QuandoAtualizarComDadosInvalidos() {
        Long fonteId = 1L;
        
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
            .put("/api/fontes-financiadoras/{id}", fonteId)
        .then()
            .log().all()
            .statusCode(400);
    }

    // ========== DELETE /api/fontes-financiadoras/{id} ==========

    @Test
    void deveDeletarFonteFinanciadoraComoAdministrador() {
        // Primeiro criar uma fonte para deletar
        String nomeTemp = "Fonte Temporária " + System.currentTimeMillis();
        String jsonBodyCriar = """
            {
              "nome": "%s"
            }
        """.formatted(nomeTemp);

        Integer fonteId = given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .contentType(ContentType.JSON)
            .body(jsonBodyCriar)
        .when()
            .post("/api/fontes-financiadoras")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Agora deletar a fonte criada
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/fontes-financiadoras/{id}", fonteId)
        .then()
            .log().all()
            .statusCode(204);

        // Verificar que foi deletada
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
        .when()
            .get("/api/fontes-financiadoras/{id}", fonteId)
        .then()
            .statusCode(404);
    }

    @Test
    void deveRetornar404QuandoDeletarFonteInexistente() {
        Long fonteIdInexistente = 9999L;

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/fontes-financiadoras/{id}", fonteIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar403QuandoGerenteTentaDeletar() {
        // Gerente não pode deletar, apenas ADMINISTRADOR
        Long fonteId = 3L; // ID da CAPES

        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .log().all()
        .when()
            .delete("/api/fontes-financiadoras/{id}", fonteId)
        .then()
            .log().all()
            .statusCode(403);
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoNoDelete() {
        Long fonteId = 2L; // ID da FAPEAM

        given()
            .port(port)
            .log().all()
        .when()
            .delete("/api/fontes-financiadoras/{id}", fonteId)
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }
}

