package edu.uea.acadmanage.service.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionSystemException;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.jdbc.BadSqlGrammarException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Map<String, String>> handleAcessoNegadoException(AcessoNegadoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(SenhaIncorretaException.class)
    public ResponseEntity<Map<String, String>> handleSenhaIncorretaException(SenhaIncorretaException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<Map<String, String>> handleConflitoException(ConflitoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("status", "CONFLICT");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(CursoComAtividadesException.class)
    public ResponseEntity<Map<String, String>> handleCursoComAtividadesException(CursoComAtividadesException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("status", "CONFLICT");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(AtividadeComEvidenciasException.class)
    public ResponseEntity<Map<String, String>> handleAtividadeComEvidenciasException(AtividadeComEvidenciasException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("status", "CONFLICT");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> detalhes = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String campo = violation.getPropertyPath() != null ? 
                        violation.getPropertyPath().toString() : "campo desconhecido";
                    String mensagem = violation.getMessage();
                    
                    // Mensagens mais amigáveis para campos comuns
                    if (campo.contains("cpf") && mensagem.contains("CPF inválido")) {
                        return "CPF inválido. Verifique se o CPF está correto.";
                    }
                    
                    return campo + ": " + mensagem;
                })
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Falha de validação nos dados enviados.");
        body.put("status", "BAD_REQUEST");
        body.put("detalhes", detalhes);
        
        // Se houver apenas um erro, retornar mensagem mais clara
        if (detalhes.size() == 1) {
            body.put("message", detalhes.get(0));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionSystemException(TransactionSystemException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Erro na transação do banco de dados");
        error.put("status", "BAD_REQUEST");
        
        // Verificar se a causa raiz é uma ConstraintViolationException
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) rootCause;
            List<String> detalhes = cve.getConstraintViolations()
                    .stream()
                    .map(violation -> {
                        String campo = violation.getPropertyPath() != null ? 
                            violation.getPropertyPath().toString() : "campo desconhecido";
                        String mensagem = violation.getMessage();
                        
                        // Mensagens mais amigáveis para campos comuns
                        if (campo.contains("cpf") && mensagem.contains("CPF inválido")) {
                            return "CPF inválido. Verifique se o CPF está correto.";
                        }
                        
                        return campo + ": " + mensagem;
                    })
                    .collect(Collectors.toList());
            
            error.put("error", "Falha de validação nos dados enviados.");
            error.put("message", detalhes.isEmpty() ? "Dados inválidos" : detalhes.get(0));
            error.put("detalhes", detalhes);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        // Se não for ConstraintViolationException, retornar erro genérico
        error.put("message", "Ocorreu um erro ao processar a operação. Verifique os dados enviados.");
        if (rootCause != null) {
            error.put("details", rootCause.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsernameNotFound(UsernameNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Sessão inválida: usuário não encontrado.");
        error.put("action", "logout");
        error.put("message", "Sua conta pode ter sido removida ou desativada. Faça login novamente.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwtException(ExpiredJwtException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Token JWT expirado");
        error.put("message", "Sua sessão expirou. Por favor, faça login novamente.");
        error.put("status", "UNAUTHORIZED");
        error.put("action", "refresh_token_required");
        
        // Informações adicionais sobre a expiração
        if (ex.getClaims() != null && ex.getClaims().getExpiration() != null) {
            error.put("expiredAt", ex.getClaims().getExpiration().toString());
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(JwtException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Token JWT inválido");
        error.put("message", "O token de autenticação fornecido é inválido ou malformado.");
        error.put("status", "UNAUTHORIZED");
        error.put("action", "login_required");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(TipoCursoEmUsoException.class)
    public ResponseEntity<Map<String, String>> handleTipoCursoEmUsoException(TipoCursoEmUsoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensagem", "Não é possível excluir o tipo de curso pois existem cursos associados.");
        error.put("detalhes", ex.getMessage());
        error.put("tipoCursoId", String.valueOf(ex.getTipoCursoId()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Erro interno: " + ex.getMessage());
        error.put("type", ex.getClass().getSimpleName());
        ex.printStackTrace(); // Log para debug
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Acesso negado: permissão insuficiente.");
        error.put("status", "FORBIDDEN");
        error.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ArquivoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleArquivoInvalidoException(ArquivoInvalidoException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Arquivo inválido");
        error.put("message", ex.getMessage());
        error.put("status", "BAD_REQUEST");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ErroProcessamentoArquivoException.class)
    public ResponseEntity<Map<String, Object>> handleErroProcessamentoArquivoException(ErroProcessamentoArquivoException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Erro ao processar arquivo");
        error.put("message", ex.getMessage());
        error.put("status", "INTERNAL_SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<Map<String, Object>> handleValidacaoException(ValidacaoException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Erro de validação");
        error.put("message", ex.getMessage());
        error.put("status", "BAD_REQUEST");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Erro de entrada/saída");
        error.put("message", "Ocorreu um erro ao processar o arquivo. Por favor, tente novamente.");
        error.put("status", "INTERNAL_SERVER_ERROR");
        error.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Violação de integridade de dados");
        error.put("message", "Não é possível realizar esta operação devido a restrições de integridade no banco de dados.");
        error.put("status", "CONFLICT");
        
        // Tentar extrair mensagem mais específica
        Throwable rootCauseObj = ex.getRootCause();
        String rootCause = rootCauseObj != null && rootCauseObj.getMessage() != null ? 
            rootCauseObj.getMessage() : ex.getMessage();
        if (rootCause != null && rootCause.contains("duplicate key")) {
            error.put("message", "Já existe um registro com os mesmos dados.");
        } else if (rootCause != null && rootCause.contains("foreign key")) {
            error.put("message", "Este registro está sendo referenciado por outros registros e não pode ser removido.");
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ErroEnvioEmailException.class)
    public ResponseEntity<Map<String, Object>> handleErroEnvioEmailException(ErroEnvioEmailException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Erro ao enviar e-mail");
        error.put("message", ex.getMessage());
        error.put("status", "SERVICE_UNAVAILABLE");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler({DataAccessException.class, SQLGrammarException.class, BadSqlGrammarException.class})
    public ResponseEntity<Map<String, Object>> handleDataAccessException(DataAccessException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Erro ao acessar o banco de dados");
        error.put("status", "INTERNAL_SERVER_ERROR");
        
        // Extrair mensagem mais amigável
        String message = "Ocorreu um erro ao processar sua solicitação. Por favor, tente novamente.";
        
        Throwable rootCause = ex.getRootCause();
        if (rootCause != null) {
            String rootMessage = rootCause.getMessage();
            if (rootMessage != null) {
                // Mensagens específicas para erros comuns
                if (rootMessage.contains("function lower(bytea) does not exist") || 
                    rootMessage.contains("function lower") && rootMessage.contains("does not exist")) {
                    message = "Erro ao realizar busca. O sistema está com problemas de compatibilidade com o banco de dados. Por favor, entre em contato com o suporte técnico.";
                } else if (rootMessage.contains("syntax error") || rootMessage.contains("SQL syntax")) {
                    message = "Erro na consulta ao banco de dados. Por favor, tente novamente ou entre em contato com o suporte técnico.";
                } else if (rootMessage.contains("relation") && rootMessage.contains("does not exist")) {
                    message = "Erro de configuração do banco de dados. Por favor, entre em contato com o suporte técnico.";
                }
            }
        }
        
        error.put("message", message);
        
        // Em desenvolvimento, incluir detalhes técnicos
        if (ex.getMessage() != null && ex.getMessage().contains("lower(bytea)")) {
            error.put("technicalDetails", "Erro de tipo de dados na consulta SQL. O campo está sendo interpretado incorretamente pelo banco de dados.");
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointerException(NullPointerException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Erro ao processar dados");
        error.put("message", "O sistema está processando dados incompletos. Isso pode ocorrer quando algumas informações ainda não foram cadastradas. Por favor, tente novamente em alguns instantes.");
        error.put("status", "INTERNAL_SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<Map<String, Object>> handleArithmeticException(ArithmeticException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Erro ao calcular métricas");
        error.put("message", "Não foi possível calcular algumas métricas devido à falta de dados. Isso é normal quando o sistema está sendo inicializado.");
        error.put("status", "INTERNAL_SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
}
