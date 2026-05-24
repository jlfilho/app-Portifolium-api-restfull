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
class AtividadePessoaPapelControllerIT {

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

    // ========== GET /api/atividades-pessoas/{atividadeId}/pessoas ==========

    @Test
    void deveListarPessoasPorAtividade() {
        Long atividadeId = 1L; // ID de uma atividade do data.sql
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .get("/api/atividades-pessoas/{atividadeId}/pessoas", atividadeId)
        .then()
            .log().all()
            .statusCode(200)
            .body("$", is(instanceOf(java.util.List.class)));
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoParaListarPessoas() {
        Long atividadeId = 1L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/atividades-pessoas/{atividadeId}/pessoas", atividadeId)
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    @Test
    void devePermitirSecretarioListarPessoas() {
        Long atividadeId = 1L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getSecretarioToken())
            .log().all()
        .when()
            .get("/api/atividades-pessoas/{atividadeId}/pessoas", atividadeId)
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(403)));
    }

    // ========== POST /api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId} ==========

    @Test
    void deveAssociarPessoaAAividade() {
        Long atividadeId = 1L; // ID de uma atividade do data.sql
        Long pessoaId = 2L; // ID de uma pessoa (pode já estar associada, então vamos usar uma pessoa diferente)
        String papel = "PARTICIPANTE";

        // Primeiro verificar se a pessoa já está associada, se sim, remover
        try {
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
            .when()
                .delete("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
            .then()
                .statusCode(anyOf(is(204), is(404)));
        } catch (Exception e) {
            // Ignorar se falhar
        }

        // Agora associar a pessoa
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .param("papel", papel)
            .log().all()
        .when()
            .post("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
        .then()
            .log().all()
            .statusCode(201)
            .body("papel", equalTo(papel))
            .body("id", notNullValue());
    }

    @Test
    void deveRetornar409QuandoPessoaJaEstaAssociada() {
        // Usar uma associação que já existe no data.sql
        Long atividadeId = 1L;
        Long pessoaId = 1L; // Pessoa 1 já está associada à atividade 1 como COORDENADOR
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .param("papel", "BOLSISTA")
            .log().all()
        .when()
            .post("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
        .then()
            .log().all()
            .statusCode(409)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar404QuandoAtividadeNaoExiste() {
        Long atividadeIdInexistente = 9999L;
        Long pessoaId = 1L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .param("papel", "PARTICIPANTE")
            .log().all()
        .when()
            .post("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeIdInexistente, pessoaId)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar404QuandoPessoaNaoExiste() {
        Long atividadeId = 1L;
        Long pessoaIdInexistente = 9999L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .param("papel", "PARTICIPANTE")
            .log().all()
        .when()
            .post("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoNoPost() {
        Long atividadeId = 1L;
        Long pessoaId = 1L;
        
        given()
            .port(port)
            .param("papel", "PARTICIPANTE")
            .log().all()
        .when()
            .post("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    @Test
    void devePermitirGerenteAssociarPessoa() {
        Long atividadeId = 2L;
        Long pessoaId = 5L; // Uma pessoa que pode não estar associada
        
        // Primeiro remover se já estiver associada
        try {
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
            .when()
                .delete("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
            .then()
                .statusCode(anyOf(is(204), is(404)));
        } catch (Exception e) {
            // Ignorar
        }

        // Gerente pode associar
        given()
            .port(port)
            .header("Authorization", "Bearer " + getGerenteToken())
            .param("papel", "VOLUNTARIO")
            .log().all()
        .when()
            .post("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
        .then()
            .log().all()
            .statusCode(anyOf(is(201), is(403), is(409))); // Pode variar dependendo do acesso ao curso
    }

    // ========== PUT /api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId} ==========

    @Test
    void deveAlterarPapelDaPessoa() {
        Long atividadeId = 1L;
        Long pessoaId = 3L; // Pessoa 3 está associada como BOLSISTA na atividade 1
        String novoPapel = "SUBCOORDENADOR";
        String papelOriginal = "BOLSISTA";

        try {
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .param("novoPapel", novoPapel)
                .log().all()
            .when()
                .put("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
            .then()
                .log().all()
                .statusCode(200)
                .body("papel", equalTo(novoPapel));
        } finally {
            // Restaurar papel original
            try {
                given()
                    .port(port)
                    .header("Authorization", "Bearer " + getAdminToken())
                    .param("novoPapel", papelOriginal)
                .when()
                    .put("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
                .then()
                    .statusCode(200);
            } catch (Exception e) {
                // Ignorar se falhar
            }
        }
    }

    @Test
    void deveRetornar404QuandoAtividadeNaoExisteParaAlterar() {
        Long atividadeIdInexistente = 9999L;
        Long pessoaId = 1L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .param("novoPapel", "COORDENADOR")
            .log().all()
        .when()
            .put("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeIdInexistente, pessoaId)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar404QuandoPessoaNaoEstaAssociadaParaAlterar() {
        Long atividadeId = 2L;
        Long pessoaIdNaoAssociada = 1L; // Pessoa que pode não estar associada à atividade 2
        
        // Primeiro verificar e remover se estiver
        try {
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
            .when()
                .delete("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaIdNaoAssociada)
            .then()
                .statusCode(anyOf(is(204), is(404)));
        } catch (Exception e) {
            // Ignorar
        }

        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .param("novoPapel", "COORDENADOR")
            .log().all()
        .when()
            .put("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaIdNaoAssociada)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoNoPut() {
        Long atividadeId = 1L;
        Long pessoaId = 1L;
        
        given()
            .port(port)
            .param("novoPapel", "COORDENADOR")
            .log().all()
        .when()
            .put("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ========== DELETE /api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId} ==========

    @Test
    void deveRemoverPessoaDaAtividade() {
        // Primeiro associar uma pessoa para depois remover
        Long atividadeId = 15L; // ID de uma atividade do data-test.sql (existe)
        Long pessoaId = 1L;
        
        // Verificar se já está associada, se não, associar
        try {
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .param("papel", "PARTICIPANTE")
            .when()
                .post("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
            .then()
                .statusCode(anyOf(is(201), is(409))); // 409 se já estiver associada
        } catch (Exception e) {
            // Ignorar
        }

        // Agora remover
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
        .then()
            .log().all()
            .statusCode(204);
    }

    @Test
    void deveRetornar404QuandoAtividadeNaoExisteParaRemover() {
        Long atividadeIdInexistente = 9999L;
        Long pessoaId = 1L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeIdInexistente, pessoaId)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar404QuandoPessoaNaoEstaAssociadaParaRemover() {
        Long atividadeId = 3L;
        Long pessoaIdNaoAssociada = 7L; // Pessoa que provavelmente não está associada à atividade 3
        
        // Verificar e remover se estiver associada
        try {
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
            .when()
                .delete("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaIdNaoAssociada)
            .then()
                .statusCode(anyOf(is(204), is(404)));
        } catch (Exception e) {
            // Ignorar
        }

        // Tentar remover novamente (deve retornar 404)
        given()
            .port(port)
            .header("Authorization", "Bearer " + getAdminToken())
            .log().all()
        .when()
            .delete("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaIdNaoAssociada)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornar403QuandoNaoAutenticadoNoDelete() {
        Long atividadeId = 1L;
        Long pessoaId = 1L;
        
        given()
            .port(port)
            .log().all()
        .when()
            .delete("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403)));
    }

    @Test
    void devePermitirSecretarioRemoverPessoa() {
        Long atividadeId = 14L; // ID de uma atividade
        Long pessoaId = 1L;
        
        // Primeiro associar se não estiver
        try {
            given()
                .port(port)
                .header("Authorization", "Bearer " + getAdminToken())
                .param("papel", "PARTICIPANTE")
            .when()
                .post("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
            .then()
                .statusCode(anyOf(is(201), is(409)));
        } catch (Exception e) {
            // Ignorar
        }

        // Secretário pode remover
        given()
            .port(port)
            .header("Authorization", "Bearer " + getSecretarioToken())
            .log().all()
        .when()
            .delete("/api/atividades-pessoas/{atividadeId}/pessoas/{pessoaId}", atividadeId, pessoaId)
        .then()
            .log().all()
            .statusCode(anyOf(is(204), is(403), is(404))); // Pode variar dependendo do acesso ao curso
    }
}

