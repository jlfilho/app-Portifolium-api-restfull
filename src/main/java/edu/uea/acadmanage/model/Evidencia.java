package edu.uea.acadmanage.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Evidencia implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String urlFoto;
    @Column(length = 500)
    private String legenda;
    private String criadoPor;
    @Column(nullable = false)
    private Integer ordem;

    @JsonIgnoreProperties("evidencias")
    @ManyToOne
    @JoinColumn(name = "atividade_id", nullable = false)
    private Atividade atividade;


    // Encapsula o acesso ao curso
    public Curso getCurso() {
        return this.atividade != null ? this.atividade.getCurso() : null;
    }

}
