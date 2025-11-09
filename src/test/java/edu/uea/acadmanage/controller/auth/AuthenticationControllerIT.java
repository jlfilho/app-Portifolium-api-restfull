package edu.uea.acadmanage.controller.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("jwt")
class AuthenticationControllerIT {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        // Configurar a porta base do REST Assured
        io.restassured.RestAssured.port = port;
    }

    @Test
    void deveAutenticarUsuarioComSucesso() {
        given()
            .port(port)
            .contentType("application/json")
            .body("""
                {
                  "username": "admin@uea.edu.br",
                  "password": "admin123"
                }
            """)
            .log().all()
        .when()
            .post("/api/auth/login")
        .then()
            .log().all()
            .statusCode(200)
            .body("token", notNullValue())
            .body("expiresIn", greaterThan(0));
    }

    @Test
    void deveRetornar401QuandoCredenciaisInvalidas() {
        given()
            .port(port)
            .contentType("application/json")
            .body("""
                {
                  "username": "admin@uea.edu.br",
                  "password": "admin1234"
                }
            """)
            .log().all()
        .when()
            .post("/api/auth/login")
        .then()
            .log().all()
            .statusCode(401)
            .body("error", containsStringIgnoringCase("invalid"));
    }
}

