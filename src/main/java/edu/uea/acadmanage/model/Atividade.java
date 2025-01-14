package edu.uea.acadmanage.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Atividade implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @Column(length = 1000)
    private String objetivo;
    @Column(length = 1000)
    private String publicoAlvo;
    @Column(nullable = false)
    private Boolean statusPublicacao;
    private String fotoCapa;
    @Column(nullable = false)
    private LocalDate dataRealizacao;

    @JsonIgnoreProperties("atividades")
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
    
    @JsonIgnoreProperties("atividades")
    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;
    
    @JsonIgnoreProperties("atividade")
    @OneToMany(mappedBy = "atividade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evidencia> evidencias;

    @JsonIgnoreProperties("atividades")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "atividade_financiadora", // Nome da tabela de junção
        joinColumns = @JoinColumn(name = "atividade_id"), // Chave estrangeira de Curso
        inverseJoinColumns = @JoinColumn(name = "financiadora_id") // Chave estrangeira de Usuario
        )
    private List<FonteFinanciadora> fontesFinanciadora;

    @OneToMany(mappedBy = "atividade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AtividadePessoaPapel> pessoas = new ArrayList<>();

    public Atividade(Long id) {
        this.id = id;
    }

    public Boolean IsPublicada() {
        return this.statusPublicacao;
    }

    public String getCoordenador() {
        for (AtividadePessoaPapel pessoa : this.pessoas) {
            if (pessoa.getPapel().equals(Papel.COORDENADOR)) {
                return pessoa.getPessoa().getNome();
            }
        }
        return null;
    }

}
