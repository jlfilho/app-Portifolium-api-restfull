package edu.uea.acadmanage.service.exception;

public class CursoComAtividadesException extends RuntimeException {

    public CursoComAtividadesException() {
        super("Não é permitido excluir cursos com atividades cadastradas.");
    }

    public CursoComAtividadesException(String message) {
        super(message);
    }
}

