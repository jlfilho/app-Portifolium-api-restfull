package edu.uea.acadmanage.service.exception;

public class ErroProcessamentoArquivoException extends RuntimeException {
    public ErroProcessamentoArquivoException(String message) {
        super(message);
    }

    public ErroProcessamentoArquivoException(String message, Throwable cause) {
        super(message, cause);
    }
}

