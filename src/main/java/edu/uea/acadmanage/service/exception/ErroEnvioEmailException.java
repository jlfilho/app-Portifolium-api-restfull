package edu.uea.acadmanage.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ErroEnvioEmailException extends RuntimeException {
    public ErroEnvioEmailException(String message) {
        super(message);
    }

    public ErroEnvioEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}

