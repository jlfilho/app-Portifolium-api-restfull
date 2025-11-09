package edu.uea.acadmanage.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.uea.acadmanage.DTO.UnidadeAcademicaDTO;
import edu.uea.acadmanage.model.UnidadeAcademica;
import edu.uea.acadmanage.repository.CursoRepository;
import edu.uea.acadmanage.repository.UnidadeAcademicaRepository;
import edu.uea.acadmanage.service.exception.ConflitoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class UnidadeAcademicaService {

    private final UnidadeAcademicaRepository unidadeAcademicaRepository;
    private final CursoRepository cursoRepository;

    public UnidadeAcademicaService(UnidadeAcademicaRepository unidadeAcademicaRepository,
            CursoRepository cursoRepository) {
        this.unidadeAcademicaRepository = unidadeAcademicaRepository;
        this.cursoRepository = cursoRepository;
    }

    public UnidadeAcademicaDTO salvar(UnidadeAcademicaDTO dto) {
        UnidadeAcademica unidade = new UnidadeAcademica();
        unidade.setNome(dto.nome());
        unidade.setDescricao(dto.descricao());

        UnidadeAcademica salvo = unidadeAcademicaRepository.save(unidade);
        return toDTO(salvo);
    }

    public UnidadeAcademicaDTO atualizar(Long id, UnidadeAcademicaDTO dto) {
        UnidadeAcademica unidade = buscarEntidade(id);
        unidade.setNome(dto.nome());
        unidade.setDescricao(dto.descricao());
        UnidadeAcademica salvo = unidadeAcademicaRepository.save(unidade);
        return toDTO(salvo);
    }

    public UnidadeAcademicaDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    public Page<UnidadeAcademicaDTO> listar(String nome, Pageable pageable) {
        Page<UnidadeAcademica> page;
        if (nome != null && !nome.isBlank()) {
            page = unidadeAcademicaRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        } else {
            page = unidadeAcademicaRepository.findAll(pageable);
        }
        return page.map(this::toDTO);
    }

    public void excluir(Long id) {
        UnidadeAcademica unidade = buscarEntidade(id);

        if (cursoRepository.existsByUnidadeAcademica_Id(unidade.getId())) {
            throw new ConflitoException("Não é possível excluir a unidade acadêmica pois existem cursos associados.");
        }

        unidadeAcademicaRepository.delete(unidade);
    }

    public UnidadeAcademica buscarEntidade(Long id) {
        return unidadeAcademicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade acadêmica não encontrada: " + id));
    }

    private UnidadeAcademicaDTO toDTO(UnidadeAcademica unidade) {
        return new UnidadeAcademicaDTO(unidade.getId(), unidade.getNome(), unidade.getDescricao());
    }
}

