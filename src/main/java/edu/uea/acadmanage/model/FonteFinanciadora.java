package edu.uea.acadmanage.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FonteFinanciadora implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "O nome é obrigatório.")
    @Column(nullable = false, unique = true)
    private String nome;

    
    @ManyToMany(mappedBy = "fontesFinanciadora", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Atividade> atividades;

    public FonteFinanciadora(Long id) {
        this.id = id;
    }

}
