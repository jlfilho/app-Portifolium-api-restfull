package edu.uea.acadmanage.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtividadePessoaId implements Serializable {
    private Long atividadeId;
    private Long pessoaId;
}