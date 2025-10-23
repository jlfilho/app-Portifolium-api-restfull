package edu.uea.acadmanage.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.uea.acadmanage.DTO.CursoDTO;
import edu.uea.acadmanage.DTO.PasswordChangeRequest;
import edu.uea.acadmanage.DTO.UsuarioDTO;
import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.Pessoa;
import edu.uea.acadmanage.model.Role;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.CursoRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;
import edu.uea.acadmanage.service.exception.SenhaIncorretaException;
import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final CursoRepository cursoRepository;
    private final edu.uea.acadmanage.repository.PessoaRepository pessoaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, RoleService roleService,
            CursoRepository cursoRepository, edu.uea.acadmanage.repository.PessoaRepository pessoaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.cursoRepository = cursoRepository;
        this.pessoaRepository = pessoaRepository;
    }



     // Listar todos os usuários (sem paginação - mantido para compatibilidade)
    public List<UsuarioDTO> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuario -> new UsuarioDTO(usuario.getId(), usuario.getPessoa().getNome(), usuario.getPessoa().getCpf(), usuario.getEmail(),
                        null,
                        usuario.getRoles().stream()
                                .map(r -> r.getNome())
                                .toList().get(0),
                        usuario.getCursos().stream()
                                .map(curso -> new CursoDTO(
                                        curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo()))
                                .toList()))
                .collect(Collectors.toList());
    }

    // Listar usuários com paginação
    public Page<UsuarioDTO> getAllUsuariosPaginados(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(usuario -> new UsuarioDTO(
                        usuario.getId(), 
                        usuario.getPessoa().getNome(), 
                        usuario.getPessoa().getCpf(), 
                        usuario.getEmail(),
                        null,
                        usuario.getRoles().stream()
                                .map(r -> r.getNome())
                                .toList().get(0),
                        usuario.getCursos().stream()
                                .map(curso -> new CursoDTO(
                                        curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo()))
                                .toList()));
    }

    // Listar usuários com paginação e filtro por nome
    public Page<UsuarioDTO> getAllUsuariosPaginadosComFiltro(String nome, Pageable pageable) {
        if (nome == null || nome.trim().isEmpty()) {
            // Se o filtro não for informado, retorna todos os usuários
            return getAllUsuariosPaginados(pageable);
        } else {
            // Se o filtro for informado, retorna usuários filtrados por nome
            return usuarioRepository.findByPessoaNomeContainingIgnoreCase(nome.trim(), pageable)
                    .map(usuario -> new UsuarioDTO(
                            usuario.getId(), 
                            usuario.getPessoa().getNome(), 
                            usuario.getPessoa().getCpf(), 
                            usuario.getEmail(),
                            null,
                            usuario.getRoles().stream()
                                    .map(r -> r.getNome())
                                    .toList().get(0),
                            usuario.getCursos().stream()
                                    .map(curso -> new CursoDTO(
                                            curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo()))
                                    .toList()));
        }
    }

    // Buscar um único usuário por ID
    public UsuarioDTO getUsuarioById(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + usuarioId));
        
        return toUsuarioDTO(usuario);
    }

    // Método para salvar um usuário
    @Transactional
    public UsuarioDTO save(UsuarioDTO usuario) {
        // Validar se role existe
        Role role = roleService.getRoleByNome(usuario.role().toUpperCase());

        // Verificar se o usuário já existe
        if (usuarioRepository.existsByEmail(usuario.email())) {
            throw new AcessoNegadoException("Usuário já existe com email: " + usuario.email());
        }

        // Verificar se CPF já existe
        if (usuario.cpf() != null && !usuario.cpf().isEmpty() && pessoaRepository.existsByCpf(usuario.cpf())) {
            throw new AcessoNegadoException("CPF já cadastrado: " + usuario.cpf());
        }

        // Buscar cursos associados
        List<Curso> cursosExistentes = fetchAssociatedCursos(usuario);

        // Criar novo usuário
        Usuario novoUsuario = new Usuario();
        novoUsuario.setPessoa(new Pessoa(null, usuario.nome(), usuario.cpf()));
        novoUsuario.setEmail(usuario.email());
        novoUsuario.setSenha(passwordEncoder.encode(usuario.senha()));
        novoUsuario.getRoles().add(role);

        // Associar cursos ao usuário
        cursosExistentes.forEach(curso -> curso.getUsuarios().add(novoUsuario));
        novoUsuario.setCursos(cursosExistentes);

        // Salvar no banco
        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        // Converter para DTO e retornar
        return toUsuarioDTO(usuarioSalvo);
    }

    // Método para atualizar um usuário
    @Transactional
    public UsuarioDTO update(Long userId, UsuarioDTO usuario) {
        // Buscar o usuário existente
        Usuario usuarioExistente = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + userId));

        // Atualizar informações básicas
        usuarioExistente.getPessoa().setNome(usuario.nome());
        
        // Atualizar CPF se fornecido e diferente do atual
        if (usuario.cpf() != null && !usuario.cpf().isEmpty()) {
            if (!usuario.cpf().equals(usuarioExistente.getPessoa().getCpf())) {
                // Verificar se o novo CPF já existe em outra pessoa
                if (pessoaRepository.existsByCpf(usuario.cpf())) {
                    throw new AcessoNegadoException("CPF já cadastrado: " + usuario.cpf());
                }
                usuarioExistente.getPessoa().setCpf(usuario.cpf());
            }
        }

        // Atualizar email se diferente do atual
        if (!usuario.email().equals(usuarioExistente.getEmail())) {
            if (usuarioRepository.existsByEmail(usuario.email())) {
                throw new AcessoNegadoException("Email já cadastrado: " + usuario.email());
            }
            usuarioExistente.setEmail(usuario.email());
        }

        // Atualizar senha, se fornecida
        if (usuario.senha() != null && !usuario.senha().isEmpty()) {
            usuarioExistente.setSenha(passwordEncoder.encode(usuario.senha()));
        }

        // Atualizar role
        Role role = roleService.getRoleByNome(usuario.role().toUpperCase());
        usuarioExistente.getRoles().clear();
        usuarioExistente.getRoles().add(role);

        // Atualizar cursos associados - remover associações antigas primeiro
        List<Curso> cursosAntigos = new ArrayList<>(usuarioExistente.getCursos());
        cursosAntigos.forEach(curso -> curso.getUsuarios().remove(usuarioExistente));
        usuarioExistente.getCursos().clear();

        // Adicionar novos cursos
        List<Curso> cursosAtualizados = fetchAssociatedCursos(usuario);
        cursosAtualizados.forEach(curso -> {
            if (!curso.getUsuarios().contains(usuarioExistente)) {
                curso.getUsuarios().add(usuarioExistente);
            }
        });
        usuarioExistente.setCursos(new ArrayList<>(cursosAtualizados));

        // Salvar usuário atualizado no banco de dados
        Usuario usuarioAtualizado = usuarioRepository.save(usuarioExistente);

        // Retornar o DTO do usuário atualizado
        return toUsuarioDTO(usuarioAtualizado);
    }

    @Transactional
    public void deleteUsuario(Long usuarioId) {
        // Buscar o usuário
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + usuarioId));
        
        // Remover associações com cursos (lado inverso do relacionamento)
        // Criar uma cópia da lista para evitar ConcurrentModificationException
        List<Curso> cursosAssociados = new ArrayList<>(usuario.getCursos());
        for (Curso curso : cursosAssociados) {
            curso.getUsuarios().remove(usuario);
        }
        
        // Limpar a lista de cursos do usuário
        usuario.getCursos().clear();
        
        // Limpar a lista de roles
        usuario.getRoles().clear();
        
        // Se a pessoa existir, limpar as atividades
        if (usuario.getPessoa() != null) {
            usuario.getPessoa().getAtividades().clear();
        }
        
        // Agora podemos deletar o usuário (Pessoa será deletada em cascade)
        usuarioRepository.delete(usuario);
    }
    
    @Transactional
    public void changePassword(Long usuarioId, PasswordChangeRequest passwordChangeRequest, String username) {
        // Buscar usuário pelo ID
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // Busca usuario logado
        Usuario usuarioLogado = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
         //Verificar se usuário tem permissão para trocar senha
         if (!usuarioLogado.getRoles().stream()
         .anyMatch(role -> role.getNome().equals("ROLE_ADMINISTRADOR")) && usuarioLogado.getId() != usuarioId) {
            throw new AcessoNegadoException("Usuário não tem permissão para alterar a senha de: " + usuario.getEmail());
        }

        // Verificar se a senha atual está correta
        if (!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), usuario.getSenha())) {
            throw new SenhaIncorretaException("A senha atual está incorreta");
        }

        // Atualizar a senha
        usuario.setSenha(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        usuarioRepository.save(usuario);
    }

    // Método para buscar um usuário pelo email
    public Usuario getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

    }

    // Verificar as permissoes do usuario autenticado
    public void checkAuthorities() {
        // Obtém o contexto de segurança atual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Imprime as authorities atribuídas ao usuário autenticado
        System.out.println("Username: " + authentication.getName());
        System.out.println("Authorities: ");
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            System.out.println(authority.getAuthority());
        }
    }

    // Método para buscar cursos associados a um usuário
    public List<CursoDTO> getCursosByUsuarioEmail(String email) {
        // Recupera o usuário do banco
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        // Recupera os cursos associados ao usuário
        return usuario.getCursos().stream()
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo()))
                .collect(Collectors.toList());
    }

    // Converte um CursoDTO para um Curso
    public Curso toCurso(CursoDTO cursoDTO) {
        if (cursoDTO == null) {
            throw new IllegalArgumentException("O CursoDTO não pode ser nulo.");
        }
        Curso curso = new Curso();
        curso.setId(cursoDTO.id());
        curso.setNome(cursoDTO.nome());
        curso.setDescricao(cursoDTO.descricao());
        return curso;
    }

    // Método lista todos os cursos associados a um usuário
    private List<Curso> fetchAssociatedCursos(UsuarioDTO usuario) {
        if ("ROLE_ADMINISTRADOR".equals(usuario.role())) {
            return cursoRepository.findAll();
        }

        return usuario.cursos().stream()
                .map(cursoDTO -> cursoRepository.findById(cursoDTO.id())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado: " + cursoDTO.id())))
                .toList();
    }

    // Método para converter um usuário para um DTO
    private UsuarioDTO toUsuarioDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getPessoa().getNome(),
                usuario.getPessoa().getCpf(),
                usuario.getEmail(),
                null,
                usuario.getRoles().stream()
                        .map(Role::getNome)
                        .findFirst()
                        .orElse("Sem Role"),
                usuario.getCursos().stream()
                        .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo()))
                        .toList());
    }


}
