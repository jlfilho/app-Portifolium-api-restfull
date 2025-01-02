package edu.uea.acadmanage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtividadePessoaPapel {

    @EmbeddedId
    private AtividadePessoaId id;

    @JsonIgnore
    @ManyToOne
    @MapsId("atividadeId") // Mapeia o atributo atividadeId do ID composto
    private Atividade atividade;

    @JsonIgnore
    @ManyToOne
    @MapsId("pessoaId") // Mapeia o atributo pessoaId do ID composto
    private Pessoa pessoa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Papel papel;
}

