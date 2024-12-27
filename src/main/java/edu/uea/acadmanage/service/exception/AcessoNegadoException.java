package edu.uea.acadmanage.service.exception;

public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }

    public AcessoNegadoException(String msg, Throwable cause) {
        super(msg, cause);
     }
}