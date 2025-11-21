package edu.uea.acadmanage.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(exclude = "usuarios")
@AllArgsConstructor
@NoArgsConstructor
public class Curso extends BaseAuditableEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 255)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "foto_capa", columnDefinition = "VARCHAR(500)")
    private String fotoCapa;

    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "tipo_curso_id")
    private TipoCurso tipoCurso;

    @JsonIgnoreProperties("cursos")
    @ManyToOne
    @JoinColumn(name = "unidade_academica_id")
    private UnidadeAcademica unidadeAcademica;

    @JsonIgnoreProperties("curso")
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Atividade> atividades;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}) 
    @JoinTable(
        name = "curso_usuario", // Nome da tabela intermediária
        joinColumns = @JoinColumn(name = "curso_id"), // Coluna que referencia Curso
        inverseJoinColumns = @JoinColumn(name = "usuario_id") // Coluna que referencia Usuario
    )
    @JsonIgnoreProperties("cursos")
    private Set<Usuario> usuarios = new HashSet<>();


    public Curso(Long id) {
        this.id = id;
    }


}
