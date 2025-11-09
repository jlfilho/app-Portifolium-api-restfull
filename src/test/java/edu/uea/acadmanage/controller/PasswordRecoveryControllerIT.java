package edu.uea.acadmanage.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import edu.uea.acadmanage.model.RecoveryCode;
import edu.uea.acadmanage.repository.RecoveryCodeRepository;
import edu.uea.acadmanage.service.EmailService;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("jwt")
class PasswordRecoveryControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private RecoveryCodeRepository recoveryCodeRepository;

    @MockBean
    private EmailService emailService; // Mocka o serviço de email para não enviar emails reais

    @BeforeEach
    void setUp() {
        // Configurar a porta base do REST Assured
        io.restassured.RestAssured.port = port;
        // Limpar códigos de recuperação antes de cada teste
        recoveryCodeRepository.deleteAll();
    }

    @Test
    void deveGerarCodigoRecuperacaoComEmailValido() {
        given()
            .port(port)
            .contentType(ContentType.URLENC)
            .param("email", "admin@uea.edu.br")
            .log().all()
        .when()
            .post("/api/recovery/generate")
        .then()
            .log().all()
            .statusCode(200)
            .body(containsString("Código de recuperação enviado para o email"));
    }

    @Test
    void deveRetornarErroQuandoEmailNaoExiste() {
        given()
            .port(port)
            .contentType(ContentType.URLENC)
            .param("email", "naoexiste@uea.edu.br")
            .log().all()
        .when()
            .post("/api/recovery/generate")
        .then()
            .log().all()
            .statusCode(401)
            .body("error", notNullValue());
    }

    @Test
    void deveRedefinirSenhaComCodigoValido() {
        String email = "admin@uea.edu.br";
        
        // Gerar código de recuperação
        given()
            .port(port)
            .contentType(ContentType.URLENC)
            .param("email", email)
        .when()
            .post("/api/recovery/generate")
        .then()
            .statusCode(200);

        // Buscar o código gerado no banco
        // Como estamos em um ambiente de teste isolado, podemos buscar qualquer código válido recente
        LocalDateTime agora = LocalDateTime.now();
        java.util.List<RecoveryCode> codigosValidos = recoveryCodeRepository.findAll().stream()
            .filter(rc -> rc.getExpirationTime().isAfter(agora))
            .toList();
        
        // Verificar se foi gerado um código
        assert !codigosValidos.isEmpty() : "Deveria ter gerado um código de recuperação";
        
        // Pegar o primeiro código válido (em ambiente de teste, é o que acabou de ser gerado)
        String recoveryCode = codigosValidos.get(0).getCode();
        String newPassword = "novaSenha123";
        
        // Redefinir senha com o código válido
        given()
            .port(port)
            .contentType(ContentType.URLENC)
            .param("email", email)
            .param("recoveryCode", recoveryCode)
            .param("newPassword", newPassword)
            .log().all()
        .when()
            .post("/api/recovery/reset-password")
        .then()
            .log().all()
            .statusCode(200)
            .body(containsString("Senha redefinida com sucesso"));
    }

    @Test
    void deveRetornarErroQuandoCodigoRecuperacaoInvalido() {
        given()
            .port(port)
            .contentType(ContentType.URLENC)
            .param("email", "admin@uea.edu.br")
            .param("recoveryCode", "codigo-inexistente-12345")
            .param("newPassword", "novaSenha123")
            .log().all()
        .when()
            .post("/api/recovery/reset-password")
        .then()
            .log().all()
            .statusCode(403)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornarErroQuandoEmailNaoExisteNoReset() {
        given()
            .port(port)
            .contentType(ContentType.URLENC)
            .param("email", "naoexiste@uea.edu.br")
            .param("recoveryCode", "algum-codigo-12345")
            .param("newPassword", "novaSenha123")
            .log().all()
        .when()
            .post("/api/recovery/reset-password")
        .then()
            .log().all()
            .statusCode(403)
            .body("error", notNullValue());
    }

    @Test
    void deveRetornarErroQuandoParametrosObrigatoriosFaltando() {
        given()
            .port(port)
            .contentType(ContentType.URLENC)
            .param("email", "admin@uea.edu.br")
            // Faltando recoveryCode e newPassword
            .log().all()
        .when()
            .post("/api/recovery/reset-password")
        .then()
            .log().all()
            .statusCode(anyOf(is(400), is(500))); // Pode variar dependendo da validação
    }
}

