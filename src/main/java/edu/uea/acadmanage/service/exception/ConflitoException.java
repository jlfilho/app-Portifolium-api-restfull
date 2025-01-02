package edu.uea.acadmanage.service.exception;

public class ConflitoException extends RuntimeException {
    public ConflitoException(String message) {
        super(message);
    }

    public ConflitoException(String msg, Throwable cause) {
        super(msg, cause);
     }

}
