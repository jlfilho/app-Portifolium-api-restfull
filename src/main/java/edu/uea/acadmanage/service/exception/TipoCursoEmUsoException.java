package edu.uea.acadmanage.service.exception;

public class TipoCursoEmUsoException extends ConflitoException {
    private final Long tipoCursoId;

    public TipoCursoEmUsoException(Long tipoCursoId) {
        super("Não é possível excluir o tipo de curso. Existem cursos associados a este tipo.");
        this.tipoCursoId = tipoCursoId;
    }

    public Long getTipoCursoId() {
        return tipoCursoId;
    }
}


