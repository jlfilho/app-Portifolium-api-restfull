package edu.uea.acadmanage.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@EqualsAndHashCode(exclude = {"usuario", "atividades"})
@ToString(exclude = {"usuario", "atividades"})
@AllArgsConstructor
@NoArgsConstructor
public class Pessoa extends BaseAuditableEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @CPF(message = "CPF inválido")
    @Column(unique = true, nullable = false)
    private String cpf;

    @OneToOne(mappedBy = "pessoa")
    @JsonIgnore
    private Usuario usuario;

    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AtividadePessoaPapel> atividades = new ArrayList<>();

    public Pessoa(Long id, String nome, String cpf) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
    }

    public String getCpf() {
        if (this.cpf == null) {
            return null;
        }
        String digits = this.cpf.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return digits.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        }
        return this.cpf;
    }

    public void setCpf(String cpf) {
        if (cpf == null) {
            this.cpf = null;
        } else {
            this.cpf = cpf.replaceAll("\\D", "");
        }
    }

    /**
     * Retorna o CPF normalizado (apenas dígitos) para uso interno.
     * Use getCpf() para obter o CPF formatado para exibição.
     */
    public String getCpfNormalizado() {
        return this.cpf;
    }

    @PrePersist
    @PreUpdate
    private void normalizarCpf() {
        if (this.cpf != null) {
            this.cpf = this.cpf.replaceAll("\\D", "");
        }
    }
}
