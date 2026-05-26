package edu.uea.acadmanage.service.exception;

import java.util.List;

public class ValidacaoException extends RuntimeException {
    private final List<String> details;

    public ValidacaoException(String message) {
        super(message);
        this.details = null;
    }

    public ValidacaoException(String message, Throwable cause) {
        super(message, cause);
        this.details = null;
    }

    public ValidacaoException(String message, List<String> details) {
        super(message);
        this.details = details;
    }

    public List<String> getDetails() {
        return details;
    }
}

