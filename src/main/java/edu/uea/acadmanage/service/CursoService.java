package edu.uea.acadmanage.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.uea.acadmanage.DTO.CursoDTO;
import edu.uea.acadmanage.DTO.PermissaoCursoDTO;
import edu.uea.acadmanage.DTO.UsuarioDTO;
import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.CursoRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    public CursoService(
        CursoRepository cursoRepository, 
        UsuarioRepository usuarioRepository) {
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Método para buscar um curso por ID
    public CursoDTO getCursoById(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getAtivo()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
    }

    // Método para buscar todos os curso
    public List<CursoDTO> getAllCursos() {
        return cursoRepository.findAll().stream()
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getAtivo()))
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
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getAtivo()))
                .toList();
    }

    // Método para buscar todos os usuários e suas permissões associados a um curso
    public List<PermissaoCursoDTO> getAllUsuarioByCurso(Long cursoId, String username) {
        if (!verificarAcessoAoCurso(username, cursoId)) {
            throw new RecursoNaoEncontradoException("Usuário não tem permissão para acessar este curso: " + cursoId);
        }

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));

        return curso.getUsuarios().stream()
        .map(usuario -> new PermissaoCursoDTO(curso.getId(), usuario.getId(), usuario.getPessoa().getNome(), usuario.getRoles().iterator().next().getNome()))
        .toList();
    }


    public CursoDTO saveCurso(CursoDTO cursoDTO, Usuario usuario) {
        // Criar uma entidade Curso a partir do DTO
        Curso novoCurso = new Curso();
        novoCurso.setNome(cursoDTO.nome());
        Set<Usuario> usuarios = this.usuarioRepository.findAllByRoleName("ROLE_ADMINISTRADOR");
        usuarios.add(usuario);
        novoCurso.setUsuarios(usuarios);

        // Salvar no banco de dados
        Curso cursoSalvo = cursoRepository.save(novoCurso);

        // Retornar um DTO com os dados do curso salvo
        return new CursoDTO(cursoSalvo.getId(), cursoSalvo.getNome(), cursoSalvo.getAtivo());
    }

    // Método para atualizar um curso
    public CursoDTO updateCurso(Long cursoId, CursoDTO cursoDTO) {
        Curso cursoExistente = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
        // Atualizando os campos permitidos
        cursoExistente.setNome(cursoDTO.nome());
        cursoExistente.setAtivo(cursoDTO.ativo());
        // Salvando no banco
        Curso cursoAtualizado = cursoRepository.save(cursoExistente);
        return new CursoDTO(cursoAtualizado.getId(), cursoAtualizado.getNome(), cursoAtualizado.getAtivo());
    }

    // Método para atualizar usuarios de um curso
    public List<PermissaoCursoDTO> adicionarUsuarioCurso(Long cursoId, Long usuarioId) {
        Curso cursoExistente = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
        Usuario usuarioExistente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + usuarioId));
        // Atualizando os campos permitidos
        cursoExistente.getUsuarios().add(usuarioExistente);
        // Salvando no banco
        Curso cursoAtualizado = cursoRepository.save(cursoExistente);
        return cursoAtualizado.getUsuarios().stream()
                .map(usuario -> new PermissaoCursoDTO(cursoAtualizado.getId(), usuario.getId(), usuario.getPessoa().getNome(), usuario.getRoles().iterator().next().getNome()))
                .toList();
    }

    // Método para atualizar um curso
    public List<PermissaoCursoDTO> removerUsuarioCurso(Long cursoId, Long usuarioId) {
        Curso cursoExistente = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
        Usuario usuarioExistente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + usuarioId));
        if(usuarioExistente.getRoles().iterator().next().getNome().equals("ROLE_ADMINISTRADOR")) {
            throw new RecursoNaoEncontradoException("Usuário administrdor não pode ser removido.");
        }
        // Atualizando os campos permitidos
        cursoExistente.getUsuarios().removeIf(usuario -> usuario.getId().equals(usuarioId));
        // Salvando no banco
        Curso cursoAtualizado = cursoRepository.save(cursoExistente);
        return cursoAtualizado.getUsuarios().stream()
                .map(usuario -> new PermissaoCursoDTO(cursoExistente.getId(), usuario.getId(), usuario.getPessoa().getNome(), usuario.getRoles().iterator().next().getNome()))
                .toList();
    }

    // Método para atualizar um curso
    public CursoDTO updateStatusCurso(Long cursoId, Boolean ativo) {
        Curso cursoExistente = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
        // Atualizando os campos permitidos
        cursoExistente.setAtivo(ativo);
        // Salvando no banco
        Curso cursoAtualizado = cursoRepository.save(cursoExistente);
        return new CursoDTO(cursoAtualizado.getId(), cursoAtualizado.getNome(), cursoAtualizado.getAtivo());
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
        Usuario usuario = this.usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        // Verifica se o cursoId está na lista de cursos associados ao usuário
        return usuario.getCursos().stream()
                .anyMatch(curso -> curso.getId().equals(cursoId));
    }

    

}
