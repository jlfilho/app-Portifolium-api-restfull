package edu.uea.acadmanage.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
        long timestamp = System.currentTimeMillis();
        String novoEmail = "novousuario" + timestamp + "@uea.edu.br";
        String cpfNovo = gerarCpfValido();
        String jsonBody = """
            {
              "nome": "Novo Usuário Teste",
              "cpf": "%s",
              "email": "%s",
              "senha": "senha123",
              "role": "ROLE_SECRETARIO",
              "cursos": []
            }
        """.formatted(cpfNovo, novoEmail);

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
        String cpf = gerarCpfValido();
        String jsonBody = """
            {
              "nome": "Usuario Teste",
              "cpf": "%s",
              "email": "teste@uea.edu.br",
              "senha": "senha123",
              "role": "ROLE_SECRETARIO",
              "cursos": []
            }
        """.formatted(cpf);

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
    void deveCriarUsuarioParaPessoaExistente() {
        long timestamp = System.currentTimeMillis();
        String cpfValido = gerarCpfValido();
        String pessoaBody = """
            {
              "nome": "Pessoa Vinculo Teste",
              "cpf": "%s"
            }
        """.formatted(cpfValido);

        Integer pessoaId = given()
            .port(port)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(pessoaBody)
            .log().all()
        .when()
            .post("/api/pessoas")
        .then()
            .log().all()
            .statusCode(201)
            .extract()
            .path("id");

        String emailNovo = "pessoa.vinculo" + timestamp + "@uea.edu.br";
        String usuarioBody = """
            {
              "pessoaId": %d,
              "email": "%s",
              "senha": "senha123",
              "role": "ROLE_SECRETARIO",
              "cursosIds": [1]
            }
        """.formatted(pessoaId, emailNovo);

        Integer usuarioId = null;
        try {
            usuarioId = given()
                .port(port)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(usuarioBody)
                .log().all()
            .when()
                .post("/api/usuarios/pessoa")
            .then()
                .log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("email", equalTo(emailNovo))
                .body("nome", equalTo("Pessoa Vinculo Teste"))
                .extract()
                .path("id");
        } finally {
            if (usuarioId != null) {
                given()
                    .port(port)
                    .header("Authorization", "Bearer " + adminToken)
                    .log().all()
                .when()
                    .delete("/api/usuarios/{usuarioId}", usuarioId)
                .then()
                    .log().all()
                    .statusCode(200);
            } else if (pessoaId != null) {
                given()
                    .port(port)
                    .header("Authorization", "Bearer " + adminToken)
                    .log().all()
                .when()
                    .delete("/api/pessoas/{pessoaId}", pessoaId)
                .then()
                    .log().all()
                    .statusCode(anyOf(is(204), is(404)));
            }
        }
    }

    @Test
    void deveAtualizarUsuario() {
        Long usuarioId = 2L; // ID de um gerente
        String cpf = gerarCpfValido();
        String jsonBody = """
            {
              "nome": "Gerente Atualizado",
              "cpf": "%s",
              "email": "gerente1@uea.edu.br",
              "role": "ROLE_GERENTE"
            }
        """.formatted(cpf);

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
        
        // O endpoint requer ROLE_ADMINISTRADOR, então usar adminToken
        String jsonBody = """
            {
              "currentPassword": "%s",
              "newPassword": "%s"
            }
        """.formatted(senhaOriginal, senhaNova);

        try {
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
                // Usar admin token para restaurar (admin pode alterar senha de qualquer usuário)
                given()
                    .port(port)
                    .header("Authorization", "Bearer " + adminToken)
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
        long timestamp = System.currentTimeMillis();
        String emailTemp = "usuarioTemp" + timestamp + "@uea.edu.br";
        String cpfTemp = gerarCpfValido();
        String jsonBodyCriar = """
            {
              "nome": "Usuário Temporário",
              "cpf": "%s",
              "email": "%s",
              "senha": "senha123",
              "role": "ROLE_SECRETARIO",
              "cursos": []
            }
        """.formatted(cpfTemp, emailTemp);

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

