package edu.uea.acadmanage.service.exception;

public class AtividadeComEvidenciasException extends RuntimeException {

    public AtividadeComEvidenciasException() {
        super("Não é permitido excluir atividades com evidências cadastradas.");
    }

    public AtividadeComEvidenciasException(String message) {
        super(message);
    }
}

