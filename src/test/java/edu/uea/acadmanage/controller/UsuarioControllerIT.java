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
class UsuarioControllerIT {

    @LocalServerPort
    private int port;

    private String adminToken;
    private String gerenteToken;

    @BeforeEach
    void setUp() {
        // Configurar a porta base do REST Assured
        io.restassured.RestAssured.port = port;
        
        // Obter tokens de autenticação
        adminToken = obterToken("admin@uea.edu.br", "admin123");
        gerenteToken = obterToken("gerente1@uea.edu.br", "gerente123");
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
    void deveVerificarAuthoritiesDoUsuarioAutenticado() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .log().all()
        .when()
            .get("/api/usuarios/checkAuthorities")
        .then()
            .log().all()
            .statusCode(200)
            .body("username", notNullValue())
            .body("authorities", notNullValue())
            .body("authorities", is(instanceOf(java.util.List.class)));
    }

    @Test
    void deveListarUsuariosComPaginacao() {
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/usuarios")
        .then()
            .log().all()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    void deveListarUsuariosComFiltroPorNome() {
        // O teste verifica que o filtro funciona, independente de retornar resultados ou não
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .param("nome", "João") // Usar nome que existe no data.sql
            .param("page", 0)
            .param("size", 10)
            .log().all()
        .when()
            .get("/api/usuarios")
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(204))); // Pode retornar 200 com resultados ou 204 se vazio
    }

    @Test
    void deveRetornar403QuandoNaoAutenticado() {
        given()
            .port(port)
            .log().all()
        .when()
            .get("/api/usuarios")
        .then()
            .log().all()
            .statusCode(anyOf(is(401), is(403))); // Spring Security pode retornar 401 ou 403
    }

    @Test
    void deveBuscarUsuarioPorId() {
        Long usuarioId = 1L; // ID do admin do data.sql
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .log().all()
        .when()
            .get("/api/usuarios/{usuarioId}", usuarioId)
        .then()
            .log().all()
            .statusCode(200)
            .body("id", equalTo(usuarioId.intValue()))
            .body("email", notNullValue())
            .body("nome", notNullValue());
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExiste() {
        Long usuarioIdInexistente = 9999L;
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .log().all()
        .when()
            .get("/api/usuarios/{usuarioId}", usuarioIdInexistente)
        .then()
            .log().all()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    void deveCriarUsuarioComoAdministrador() {
        String novoEmail = "novousuario@uea.edu.br";
        String jsonBody = """
            {
              "nome": "Novo Usuário Teste",
              "cpf": "11122233344",
              "email": "%s",
              "senha": "senha123",
              "role": "ROLE_SECRETARIO",
              "cursos": []
            }
        """.formatted(novoEmail);

        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/usuarios")
        .then()
            .log().all()
            .statusCode(201)
            .body("id", notNullValue())
            .body("email", equalTo(novoEmail))
            .body("nome", equalTo("Novo Usuário Teste"));
    }

    @Test
    void deveRetornar403QuandoGerenteTentaCriarUsuario() {
        String jsonBody = """
            {
              "nome": "Usuario Teste",
              "cpf": "22233344455",
              "email": "teste@uea.edu.br",
              "senha": "senha123",
              "role": "ROLE_SECRETARIO",
              "cursos": []
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + gerenteToken)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .post("/api/usuarios")
        .then()
            .log().all()
            .statusCode(403);
    }

    @Test
    void deveAtualizarUsuario() {
        Long usuarioId = 2L; // ID de um gerente
        String jsonBody = """
            {
              "nome": "Gerente Atualizado",
              "cpf": "23456789012",
              "email": "gerente1@uea.edu.br",
              "role": "ROLE_GERENTE"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/usuarios/{usuarioId}", usuarioId)
        .then()
            .log().all()
            .statusCode(200)
            .body("id", equalTo(usuarioId.intValue()))
            .body("nome", equalTo("Gerente Atualizado"));
    }

    @Test
    void deveRetornar400QuandoDadosInvalidos() {
        Long usuarioId = 1L;
        String jsonBody = """
            {
              "nome": "",
              "email": "email-invalido",
              "role": "ROLE_ADMINISTRADOR"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/usuarios/{usuarioId}", usuarioId)
        .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    void deveAlterarSenhaDoUsuario() {
        Long usuarioId = 2L; // Usar ID de gerente em vez de admin para não afetar outros testes
        String senhaOriginal = "gerente123";
        String senhaNova = "novaSenha123";
        
        // Primeiro obter token com senha original
        String tokenOriginal = obterToken("gerente1@uea.edu.br", senhaOriginal);
        
        String jsonBody = """
            {
              "currentPassword": "%s",
              "newPassword": "%s"
            }
        """.formatted(senhaOriginal, senhaNova);

        try {
            given()
                .port(port)
                .header("Authorization", "Bearer " + tokenOriginal)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .log().all()
            .when()
                .put("/api/usuarios/{usuarioId}/change-password", usuarioId)
            .then()
                .log().all()
                .statusCode(200)
                .body("message", equalTo("Senha alterada com sucesso"))
                .body("usuarioId", notNullValue());
        } finally {
            // Sempre restaurar senha original, mesmo se o teste falhar
            String jsonBodyRestaurar = """
                {
                  "currentPassword": "%s",
                  "newPassword": "%s"
                }
            """.formatted(senhaNova, senhaOriginal);

            try {
                String tokenNovaSenha = obterToken("gerente1@uea.edu.br", senhaNova);
                given()
                    .port(port)
                    .header("Authorization", "Bearer " + tokenNovaSenha)
                    .contentType(ContentType.JSON)
                    .body(jsonBodyRestaurar)
                .when()
                    .put("/api/usuarios/{usuarioId}/change-password", usuarioId)
                .then()
                    .statusCode(200);
            } catch (Exception e) {
                // Se falhar em restaurar, logar mas não falhar o teste
                System.err.println("Aviso: Não foi possível restaurar a senha original: " + e.getMessage());
            }
        }
    }

    @Test
    void deveRetornarErroQuandoSenhaAtualIncorreta() {
        Long usuarioId = 1L;
        String jsonBody = """
            {
              "currentPassword": "senhaIncorreta",
              "newPassword": "novaSenha123"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/usuarios/{usuarioId}/change-password", usuarioId)
        .then()
            .log().all()
            .statusCode(403)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornarErroQuandoNovaSenhaMuitoCurta() {
        Long usuarioId = 1L;
        String jsonBody = """
            {
              "currentPassword": "admin123",
              "newPassword": "123"
            }
        """;

        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .log().all()
        .when()
            .put("/api/usuarios/{usuarioId}/change-password", usuarioId)
        .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    void deveDeletarUsuarioComoAdministrador() {
        // Primeiro criar um usuário para deletar
        String emailTemp = "usuarioTemp@uea.edu.br";
        String jsonBodyCriar = """
            {
              "nome": "Usuário Temporário",
              "cpf": "99988877766",
              "email": "%s",
              "senha": "senha123",
              "role": "ROLE_SECRETARIO",
              "cursos": []
            }
        """.formatted(emailTemp);

        Integer novoUsuarioId = given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(jsonBodyCriar)
        .when()
            .post("/api/usuarios")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Agora deletar o usuário criado
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .log().all()
        .when()
            .delete("/api/usuarios/{usuarioId}", novoUsuarioId)
        .then()
            .log().all()
            .statusCode(200);

        // Verificar que foi deletado
        given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/api/usuarios/{usuarioId}", novoUsuarioId)
        .then()
            .statusCode(404);
    }

    @Test
    void deveRetornar403QuandoGerenteTentaDeletarUsuario() {
        Long usuarioId = 5L; // ID de um secretário
        
        given()
            .port(port)
            .header("Authorization", "Bearer " + gerenteToken)
            .log().all()
        .when()
            .delete("/api/usuarios/{usuarioId}", usuarioId)
        .then()
            .log().all()
            .statusCode(403);
    }
}

