package edu.uea.acadmanage.service.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.exception.SQLGrammarException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import edu.uea.acadmanage.DTO.ApiErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso nao encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler({AcessoNegadoException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleAcessoNegado(
            Exception ex,
            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado", "Voce nao tem permissao para realizar esta acao.", request);
    }

    @ExceptionHandler(SenhaIncorretaException.class)
    public ResponseEntity<ApiErrorResponse> handleSenhaIncorreta(
            SenhaIncorretaException ex,
            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Senha incorreta", ex.getMessage(), request);
    }

    @ExceptionHandler({
            ConflitoException.class,
            CursoComAtividadesException.class,
            AtividadeComEvidenciasException.class,
            TipoCursoEmUsoException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflito(
            RuntimeException ex,
            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflito", ex.getMessage(), request);
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ApiErrorResponse> handleValidacao(
            ValidacaoException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Erro de validacao", ex.getMessage(), request, ex.getDetails(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        String message = details.isEmpty()
                ? "Dados invalidos. Verifique os campos enviados."
                : details.get(0);

        return build(HttpStatus.BAD_REQUEST, "Erro de validacao", message, request, details, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String field = violation.getPropertyPath() != null
                            ? violation.getPropertyPath().toString()
                            : "campo desconhecido";
                    return field + ": " + violation.getMessage();
                })
                .toList();

        String message = details.isEmpty()
                ? "Dados invalidos. Verifique os campos enviados."
                : details.get(0);

        return build(HttpStatus.BAD_REQUEST, "Erro de validacao", message, request, details, null);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiErrorResponse> handleTransactionSystemException(
            TransactionSystemException ex,
            HttpServletRequest request) {
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof ConstraintViolationException constraintViolationException) {
            return handleConstraintViolationException(constraintViolationException, request);
        }

        return build(
                HttpStatus.BAD_REQUEST,
                "Erro na transacao",
                "Ocorreu um erro ao processar a operacao. Verifique os dados enviados.",
                request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request) {
        return build(
                HttpStatus.UNAUTHORIZED,
                "Sessao invalida",
                "Sua conta pode ter sido removida ou desativada. Faca login novamente.",
                request,
                null,
                "logout");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiErrorResponse> handleExpiredJwtException(
            ExpiredJwtException ex,
            HttpServletRequest request) {
        return build(
                HttpStatus.UNAUTHORIZED,
                "Token JWT expirado",
                "Sua sessao expirou. Faca login novamente.",
                request,
                null,
                "refresh_token_required");
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> handleJwtException(
            JwtException ex,
            HttpServletRequest request) {
        return build(
                HttpStatus.UNAUTHORIZED,
                "Token JWT invalido",
                "O token de autenticacao fornecido e invalido ou malformado.",
                request,
                null,
                "login_required");
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciais invalidas", "Usuario ou senha invalidos.", request);
    }

    @ExceptionHandler(ArquivoInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleArquivoInvalidoException(
            ArquivoInvalidoException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Arquivo invalido", ex.getMessage(), request);
    }

    @ExceptionHandler(ErroProcessamentoArquivoException.class)
    public ResponseEntity<ApiErrorResponse> handleErroProcessamentoArquivoException(
            ErroProcessamentoArquivoException ex,
            HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar arquivo", ex.getMessage(), request);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiErrorResponse> handleIOException(
            IOException ex,
            HttpServletRequest request) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro de entrada/saida",
                "Ocorreu um erro ao processar o arquivo. Tente novamente.",
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        String message = resolveDataIntegrityMessage(ex);
        return build(HttpStatus.CONFLICT, "Violacao de integridade de dados", message, request);
    }

    @ExceptionHandler(ErroEnvioEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleErroEnvioEmailException(
            ErroEnvioEmailException ex,
            HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Erro ao enviar e-mail", ex.getMessage(), request);
    }

    @ExceptionHandler({DataAccessException.class, SQLGrammarException.class, BadSqlGrammarException.class})
    public ResponseEntity<ApiErrorResponse> handleDataAccessException(
            Exception ex,
            HttpServletRequest request) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao acessar o banco de dados",
                resolveDataAccessMessage(ex),
                request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Requisicao invalida", ex.getMessage(), request);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiErrorResponse> handleNullPointerException(
            NullPointerException ex,
            HttpServletRequest request) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao processar dados",
                "O sistema recebeu dados incompletos. Tente novamente.",
                request);
    }

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<ApiErrorResponse> handleArithmeticException(
            ArithmeticException ex,
            HttpServletRequest request) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao calcular metricas",
                "Nao foi possivel calcular algumas metricas devido a falta de dados.",
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        ex.printStackTrace();
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente.",
                request);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request) {
        return build(status, error, message, request, null, null);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request,
            List<String> details,
            String action) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.name(),
                error,
                message != null && !message.isBlank() ? message : error,
                request != null ? request.getRequestURI() : null,
                details,
                action);

        return ResponseEntity.status(status).body(body);
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException ex) {
        String rootMessage = getRootMessage(ex);
        if (rootMessage.contains("duplicate") || rootMessage.contains("unique")) {
            return "Ja existe um registro com os mesmos dados.";
        }
        if (rootMessage.contains("foreign key") || rootMessage.contains("constraint")) {
            return "Nao e possivel realizar esta operacao porque existem registros relacionados.";
        }
        return "Nao e possivel realizar esta operacao devido a restricoes de integridade no banco de dados.";
    }

    private String resolveDataAccessMessage(Exception ex) {
        String rootMessage = getRootMessage(ex);
        if (rootMessage.contains("function lower") && rootMessage.contains("does not exist")) {
            return "Erro ao realizar busca por incompatibilidade com o banco de dados.";
        }
        if (rootMessage.contains("syntax error") || rootMessage.contains("sql syntax")) {
            return "Erro na consulta ao banco de dados. Tente novamente.";
        }
        if (rootMessage.contains("relation") && rootMessage.contains("does not exist")) {
            return "Erro de configuracao do banco de dados.";
        }
        return "Ocorreu um erro ao acessar o banco de dados. Tente novamente.";
    }

    private String getRootMessage(Exception ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String message = root.getMessage();
        return message != null ? message.toLowerCase() : "";
    }
}
