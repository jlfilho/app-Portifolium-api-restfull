package edu.uea.acadmanage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.uea.acadmanage.DTO.CursoDTO;
import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.CursoRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final UsuarioRepository usuarioRepository;

    public CursoService(
        CursoRepository cursoRepository, 
        UsuarioRepository usuarioRepository,
        CustomUserDetailsService customUserDetailsService) {
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.customUserDetailsService = customUserDetailsService;
    }

    // Método para buscar um curso por ID
    public CursoDTO getCursoById(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
    }

    // Método para buscar todos os curso
    public List<CursoDTO> getAllCursos() {
        return cursoRepository.findAll().stream()
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome()))
                .toList();
    }

    // Método para buscar cursos associados a um usuário
    public List<CursoDTO> getCursosByUsuarioId(Long usuarioId) {
        // Verificar existência do usuário e buscar cursos em uma única operação
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + usuarioId);
        }

        // Buscar cursos associados ao usuário
        return cursoRepository.findCursosByUsuarioId(usuarioId).stream()
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome()))
                .toList();
    }


    public CursoDTO saveCurso(CursoDTO cursoDTO) {
        // Criar uma entidade Curso a partir do DTO
        Curso novoCurso = new Curso();
        novoCurso.setNome(cursoDTO.nome());

        // Salvar no banco de dados
        Curso cursoSalvo = cursoRepository.save(novoCurso);

        // Retornar um DTO com os dados do curso salvo
        return new CursoDTO(cursoSalvo.getId(), cursoSalvo.getNome());
    }

    // Método para atualizar um curso
    public CursoDTO updateCurso(Long cursoId, CursoDTO cursoDTO) {
        Curso cursoExistente = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
        // Atualizando os campos permitidos
        cursoExistente.setNome(cursoDTO.nome());
        // Salvando no banco
        Curso cursoAtualizado = cursoRepository.save(cursoExistente);
        return new CursoDTO(cursoAtualizado.getId(), cursoAtualizado.getNome());
    }

    // Método para excluir um curso
    public void excluirCurso(Long cursoId) {
        if (!cursoRepository.existsById(cursoId)) {
            throw new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId);
        }
        cursoRepository.deleteById(cursoId);
    }

    // Método para verificar se um curso existe    
    public boolean verificarSeCursoExiste(Long cursoId) {
        return cursoRepository.existsById(cursoId);
    }

    // Método para verificar se um usuário tem acesso a um curso
    public boolean verificarAcessoAoCurso(String email, Long cursoId) {
        // Recupera o usuário do banco
        Usuario usuario = customUserDetailsService.getUsuarioByEmail(email);

        // Verifica se o cursoId está na lista de cursos associados ao usuário
        return usuario.getCursos().stream()
                .anyMatch(curso -> curso.getId().equals(cursoId));
    }

    

}
