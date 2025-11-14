package edu.uea.acadmanage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.AtividadePessoaId;
import edu.uea.acadmanage.model.AtividadePessoaPapel;
import edu.uea.acadmanage.model.Pessoa;
import edu.uea.acadmanage.model.Papel;

@Repository
public interface AtividadePessoaPapelRepository extends JpaRepository<AtividadePessoaPapel, AtividadePessoaId> {

    /**
     * Verifica se uma pessoa já está associada a uma atividade.
     * 
     * @param atividade A atividade a ser verificada.
     * @param pessoa A pessoa a ser verificada.
     * @return true se a associação já existir, false caso contrário.
     */
    boolean existsByAtividadeAndPessoa(Atividade atividade, Pessoa pessoa);

    /**
     * Lista todas as associações de pessoas a uma atividade específica.
     * 
     * @param atividadeId O ID da atividade.
     * @return Lista de associações encontradas.
     */
    @Query("SELECT ap FROM AtividadePessoaPapel ap WHERE ap.atividade.id = :atividadeId")
    List<AtividadePessoaPapel> findByAtividadeId(@Param("atividadeId") Long atividadeId);

    @Query("""
        SELECT ap.atividade.id, COUNT(ap)
        FROM AtividadePessoaPapel ap
        WHERE ap.atividade.id IN :atividadeIds
          AND ap.papel IN :papeis
        GROUP BY ap.atividade.id
        """)
    List<Object[]> countParticipantesByAtividadeIds(@Param("atividadeIds") List<Long> atividadeIds,
                                                   @Param("papeis") List<Papel> papeis);
}

