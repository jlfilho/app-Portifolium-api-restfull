package edu.uea.acadmanage.service.exception;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public RecursoNaoEncontradoException(String msg, Throwable cause) {
        super(msg, cause);
     }
}
