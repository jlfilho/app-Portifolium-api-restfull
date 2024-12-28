package edu.uea.acadmanage.service.exception;

public class SenhaIncorretaException extends RuntimeException {
    public SenhaIncorretaException(String mensagem) {
        super(mensagem);
    }

    public SenhaIncorretaException(String msg, Throwable cause) {
        super(msg, cause);
     }
}