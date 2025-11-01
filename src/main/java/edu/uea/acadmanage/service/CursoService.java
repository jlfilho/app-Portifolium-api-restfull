package edu.uea.acadmanage.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

import edu.uea.acadmanage.DTO.CursoDTO;
import edu.uea.acadmanage.DTO.PermissaoCursoDTO;
import edu.uea.acadmanage.config.FileStorageProperties;
import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.TipoCurso;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.CursoRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import edu.uea.acadmanage.service.exception.ConflitoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoCursoService tipoCursoService;
    private final Path fileStorageLocation;
    private final String baseStorageLocation;

    public CursoService(
        CursoRepository cursoRepository, 
        UsuarioRepository usuarioRepository,
        TipoCursoService tipoCursoService,
        FileStorageProperties fileStorageProperties) throws IOException {
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tipoCursoService = tipoCursoService;
        this.baseStorageLocation = "/fotos-capa";
        this.fileStorageLocation = Paths.get(fileStorageProperties.getStorageLocation() + this.baseStorageLocation)
                .toAbsolutePath().normalize();
        Files.createDirectories(this.fileStorageLocation);
    }

    // Método para buscar um curso por ID
    public CursoDTO getCursoById(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo(), curso.getTipoCurso() != null ? curso.getTipoCurso().getCodigo() : null))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
    }

    // Método para buscar todos os curso
    public List<CursoDTO> getAllCursos() {
        return cursoRepository.findAll().stream()
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo(), curso.getTipoCurso() != null ? curso.getTipoCurso().getCodigo() : null))
                .toList();
    }

    // Método para buscar todos os cursos com paginação
    public Page<CursoDTO> getAllCursosPaginado(Pageable pageable) {
        return cursoRepository.findAll(pageable)
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo(), curso.getTipoCurso() != null ? curso.getTipoCurso().getCodigo() : null));
    }

    // Método para buscar todos os cursos com paginação e filtro por status
    public Page<CursoDTO> getAllCursosPaginadoComFiltro(Boolean ativo, Pageable pageable) {
        if (ativo == null) {
            // Se o filtro não for informado, retorna todos os cursos
            return cursoRepository.findAll(pageable)
                    .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo(), curso.getTipoCurso() != null ? curso.getTipoCurso().getCodigo() : null));
        } else {
            // Se o filtro for informado, retorna cursos filtrados por status
            return cursoRepository.findByAtivo(ativo, pageable)
                    .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo(), curso.getTipoCurso() != null ? curso.getTipoCurso().getCodigo() : null));
        }
    }

    // Método para buscar todos os cursos com paginação e filtros por status e nome
    public Page<CursoDTO> getAllCursosPaginadoComFiltros(Boolean ativo, String nome, Pageable pageable) {
        Page<Curso> cursos;
        
        if (ativo != null && nome != null && !nome.trim().isEmpty()) {
            // Filtrar por status E nome
            cursos = cursoRepository.findByAtivoAndNomeContainingIgnoreCase(ativo, nome.trim(), pageable);
        } else if (ativo != null) {
            // Filtrar apenas por status
            cursos = cursoRepository.findByAtivo(ativo, pageable);
        } else if (nome != null && !nome.trim().isEmpty()) {
            // Filtrar apenas por nome
            cursos = cursoRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        } else {
            // Sem filtros, retorna todos
            cursos = cursoRepository.findAll(pageable);
        }
        
        return cursos.map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo(), curso.getTipoCurso() != null ? curso.getTipoCurso().getCodigo() : null));
    }

    // Método para buscar cursos associados a um usuário
    public List<CursoDTO> getCursosByUsuarioId(Long usuarioId) {
        // Verificar existência do usuário e buscar cursos em uma única operação
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + usuarioId);
        }

        // Buscar cursos associados ao usuário
        return cursoRepository.findCursosByUsuarioId(usuarioId).stream()
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo(), curso.getTipoCurso() != null ? curso.getTipoCurso().getCodigo() : null))
                .toList();
    }

    // Método para buscar cursos associados a um usuário com paginação
    public Page<CursoDTO> getCursosByUsuarioIdPaginado(Long usuarioId, Pageable pageable) {
        // Verificar existência do usuário
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + usuarioId);
        }

        // Buscar cursos associados ao usuário com paginação
        return cursoRepository.findCursosByUsuarioIdPaginado(usuarioId, pageable)
                .map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo(), curso.getTipoCurso() != null ? curso.getTipoCurso().getCodigo() : null));
    }

    // Método para buscar cursos associados a um usuário com paginação e filtros
    public Page<CursoDTO> getCursosByUsuarioIdPaginadoComFiltros(Long usuarioId, Boolean ativo, String nome, Pageable pageable) {
        // Verificar existência do usuário
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + usuarioId);
        }

        Page<Curso> cursos;
        
        if (ativo != null && nome != null && !nome.trim().isEmpty()) {
            // Filtrar por status E nome
            cursos = cursoRepository.findCursosByUsuarioIdAndAtivoAndNomeContaining(usuarioId, ativo, nome.trim(), pageable);
        } else if (ativo != null) {
            // Filtrar apenas por status
            cursos = cursoRepository.findCursosByUsuarioIdAndAtivo(usuarioId, ativo, pageable);
        } else if (nome != null && !nome.trim().isEmpty()) {
            // Filtrar apenas por nome
            cursos = cursoRepository.findCursosByUsuarioIdAndNomeContaining(usuarioId, nome.trim(), pageable);
        } else {
            // Sem filtros, retorna todos os cursos do usuário
            cursos = cursoRepository.findCursosByUsuarioIdPaginado(usuarioId, pageable);
        }
        
        return cursos.map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getDescricao(), curso.getFotoCapa(), curso.getAtivo(), curso.getTipoCurso() != null ? curso.getTipoCurso().getCodigo() : null));
    }

    // Método para buscar todos os usuários e suas permissões associados a um curso
    public List<PermissaoCursoDTO> getAllUsuarioByCurso(Long cursoId, String username) {
        // Buscar usuário logado
        Usuario usuarioLogado = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        
        // Verificar se é ADMINISTRADOR
        boolean isAdmin = usuarioLogado.getRoles().stream()
                .anyMatch(role -> role.getNome().equals("ROLE_ADMINISTRADOR"));
        
        // Se NÃO for admin, verificar se tem acesso ao curso
        if (!isAdmin && !verificarAcessoAoCurso(username, cursoId)) {
            throw new RecursoNaoEncontradoException("Usuário não tem permissão para acessar este curso: " + cursoId);
        }

        // Buscar o curso
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));

        // Retornar usuários do curso com tratamento seguro de roles
        return curso.getUsuarios().stream()
                .map(usuario -> new PermissaoCursoDTO(
                        curso.getId(), 
                        usuario.getId(), 
                        usuario.getPessoa().getNome(), 
                        getPrimaryRole(usuario)))
                .toList();
    }
    
    // Método auxiliar para obter a role principal do usuário de forma segura
    private String getPrimaryRole(Usuario usuario) {
        return usuario.getRoles().stream()
                .map(role -> role.getNome())
                .findFirst()
                .orElse("SEM_ROLE");
    }


    public CursoDTO saveCurso(CursoDTO cursoDTO, Usuario usuario) {
        // Validar que o nome não seja nulo ou vazio
        if (cursoDTO.nome() == null || cursoDTO.nome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do curso é obrigatório");
        }
        
        // Criar uma entidade Curso a partir do DTO
        Curso novoCurso = new Curso();
        novoCurso.setNome(cursoDTO.nome());
        novoCurso.setDescricao(cursoDTO.descricao());
        novoCurso.setFotoCapa(cursoDTO.fotoCapa());
        if (cursoDTO.tipo() == null) {
            throw new IllegalArgumentException("O tipo do curso é obrigatório");
        }
        TipoCurso tipoCurso = tipoCursoService.recuperarPorCodigo(cursoDTO.tipo());
        novoCurso.setTipoCurso(tipoCurso);
        Set<Usuario> usuarios = this.usuarioRepository.findAllByRoleName("ROLE_ADMINISTRADOR");
        usuarios.add(usuario);
        novoCurso.setUsuarios(usuarios);

        // Salvar no banco de dados
        Curso cursoSalvo = cursoRepository.save(novoCurso);

        // Retornar um DTO com os dados do curso salvo
        return new CursoDTO(cursoSalvo.getId(), cursoSalvo.getNome(), cursoSalvo.getDescricao(), cursoSalvo.getFotoCapa(), cursoSalvo.getAtivo(), cursoSalvo.getTipoCurso() != null ? cursoSalvo.getTipoCurso().getCodigo() : null);
    }

    // Método para atualizar um curso
    public CursoDTO updateCurso(Long cursoId, CursoDTO cursoDTO) {
        // Validar que o nome não seja nulo ou vazio
        if (cursoDTO.nome() == null || cursoDTO.nome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do curso é obrigatório");
        }
        
        Curso cursoExistente = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
        // Atualizando os campos permitidos
        cursoExistente.setNome(cursoDTO.nome());
        cursoExistente.setDescricao(cursoDTO.descricao());
        cursoExistente.setFotoCapa(cursoDTO.fotoCapa());
        if (cursoDTO.tipo() != null) {
            TipoCurso tipoCurso = tipoCursoService.recuperarPorCodigo(cursoDTO.tipo());
            cursoExistente.setTipoCurso(tipoCurso);
        }
        cursoExistente.setAtivo(cursoDTO.ativo());
        // Salvando no banco
        Curso cursoAtualizado = cursoRepository.save(cursoExistente);
        return new CursoDTO(cursoAtualizado.getId(), cursoAtualizado.getNome(), cursoAtualizado.getDescricao(), cursoAtualizado.getFotoCapa(), cursoAtualizado.getAtivo(), cursoAtualizado.getTipoCurso() != null ? cursoAtualizado.getTipoCurso().getCodigo() : null);
    }

    // Método para adicionar usuário a um curso
    @Transactional
    public List<PermissaoCursoDTO> adicionarUsuarioCurso(Long cursoId, Long usuarioId) {
        // Buscar curso
        Curso cursoExistente = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
        
        // Buscar usuário
        Usuario usuarioExistente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + usuarioId));
        
        // Verificar se o usuário já está associado ao curso
        if (cursoExistente.getUsuarios().contains(usuarioExistente)) {
            throw new ConflitoException("O usuário já está associado a este curso.");
        }
        
        // Adicionar usuário ao curso (lado owner)
        cursoExistente.getUsuarios().add(usuarioExistente);
        
        // Adicionar curso ao usuário (lado inverse) - IMPORTANTE para relacionamento bidirecional
        if (!usuarioExistente.getCursos().contains(cursoExistente)) {
            usuarioExistente.getCursos().add(cursoExistente);
        }
        
        // Salvar ambos os lados do relacionamento
        cursoRepository.save(cursoExistente);
        usuarioRepository.save(usuarioExistente);
        
        // Retornar lista atualizada de usuários do curso
        return cursoExistente.getUsuarios().stream()
                .map(usuario -> new PermissaoCursoDTO(
                    cursoExistente.getId(), 
                    usuario.getId(), 
                    usuario.getPessoa().getNome(), 
                    usuario.getRoles().iterator().next().getNome()))
                .toList();
    }

    // Método para remover usuário de um curso
    @Transactional
    public List<PermissaoCursoDTO> removerUsuarioCurso(Long cursoId, Long usuarioId) {
        // Buscar curso
        Curso cursoExistente = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
        
        // Buscar usuário
        Usuario usuarioExistente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + usuarioId));
        
        // Verificar se o usuário é ADMINISTRADOR (não pode ser removido)
        boolean isAdmin = usuarioExistente.getRoles().stream()
                .anyMatch(role -> role.getNome().equals("ROLE_ADMINISTRADOR"));
        
        if (isAdmin) {
            throw new ConflitoException("Usuário administrador não pode ser removido do curso.");
        }
        
        // Verificar se o usuário está realmente associado ao curso
        if (!cursoExistente.getUsuarios().contains(usuarioExistente)) {
            throw new ConflitoException("O usuário não está associado a este curso.");
        }
        
        // Remover usuário do curso (lado owner)
        cursoExistente.getUsuarios().remove(usuarioExistente);
        
        // Remover curso do usuário (lado inverse) - IMPORTANTE para relacionamento bidirecional
        usuarioExistente.getCursos().remove(cursoExistente);
        
        // Salvar ambos os lados do relacionamento
        cursoRepository.save(cursoExistente);
        usuarioRepository.save(usuarioExistente);
        
        // Retornar lista atualizada de usuários do curso
        return cursoExistente.getUsuarios().stream()
                .map(usuario -> new PermissaoCursoDTO(
                    cursoExistente.getId(), 
                    usuario.getId(), 
                    usuario.getPessoa().getNome(), 
                    usuario.getRoles().iterator().next().getNome()))
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
        return new CursoDTO(cursoAtualizado.getId(), cursoAtualizado.getNome(), cursoAtualizado.getDescricao(), cursoAtualizado.getFotoCapa(), cursoAtualizado.getAtivo(), cursoAtualizado.getTipoCurso() != null ? cursoAtualizado.getTipoCurso().getCodigo() : null);
    }

    // Método para excluir um curso
    @Transactional
    public void excluirCurso(Long cursoId) {
        // Verificar se o curso existe
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));
        
        // Verificar se o curso tem atividades associadas
        if (curso.getAtividades() != null && !curso.getAtividades().isEmpty()) {
            throw new ConflitoException("Não é possível excluir o curso. Existem " + 
                curso.getAtividades().size() + " atividade(s) associada(s) a este curso.");
        }
        
        // Remover associações com usuários antes de deletar
        List<Usuario> usuariosAssociados = new ArrayList<>(curso.getUsuarios());
        for (Usuario usuario : usuariosAssociados) {
            usuario.getCursos().remove(curso);
        }
        curso.getUsuarios().clear();
        
        // Tentar deletar o curso
        try {
            cursoRepository.delete(curso);
        } catch (DataIntegrityViolationException e) {
            throw new ConflitoException("Não é possível excluir o curso. Existem registros dependentes associados.");
        }
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

    // Método para atualizar uma foto de capa
    public CursoDTO atualizarFotoCapa(Long cursoId, MultipartFile file, String username) throws IOException {
        // Verificar se o curso existe
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));

        // Verificar se o usuário tem permissão para atualizar a foto de capa
        if (!verificarAcessoAoCurso(username, cursoId)) {
            throw new AcessoNegadoException("Usuário não tem permissão para atualizar a foto de capa deste curso.");
        }

        // Excluir foto anterior se existir
        if (curso.getFotoCapa() != null && !curso.getFotoCapa().isEmpty()) {
            excluirImagem(curso.getFotoCapa());
        }

        // Salvar nova foto
        String fotoCapaPath = salvarImagem(curso, file);
        curso.setFotoCapa(fotoCapaPath);

        // Salvar curso atualizado
        Curso cursoAtualizado = cursoRepository.save(curso);

        return new CursoDTO(cursoAtualizado.getId(), cursoAtualizado.getNome(), cursoAtualizado.getDescricao(), cursoAtualizado.getFotoCapa(), cursoAtualizado.getAtivo(), cursoAtualizado.getTipoCurso() != null ? cursoAtualizado.getTipoCurso().getCodigo() : null);
    }

    // Método para baixar uma foto de capa
    public Resource downloadFotoCapa(Long cursoId, String username) throws IOException {
        // Verificar se o curso existe
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId));

        // Verificar se o usuário tem permissão para baixar a foto de capa
        if (!verificarAcessoAoCurso(username, cursoId)) {
            throw new AcessoNegadoException("Usuário não tem permissão para baixar a foto de capa deste curso.");
        }

        if (curso.getFotoCapa() == null || curso.getFotoCapa().isEmpty()) {
            throw new RecursoNaoEncontradoException("Foto de capa não encontrada para este curso.");
        }

        Path filePath = this.fileStorageLocation.resolve(curso.getFotoCapa()).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return resource;
        } else {
            throw new RecursoNaoEncontradoException("Arquivo não encontrado: " + curso.getFotoCapa());
        }
    }

    // Método para validar se o arquivo é uma imagem válida
    private Boolean validarImagem(MultipartFile file) {
        // Verificar se o arquivo enviado é uma imagem JPG ou PNG
        Set<String> allowedContentTypes = Set.of("image/jpg", "image/jpeg", "image/png");
        if (!allowedContentTypes.contains(Objects.requireNonNullElse(file.getContentType(), "").toLowerCase())) {
            throw new IllegalArgumentException("O arquivo enviado deve ser um JPG, JPEG ou PNG válido.");
        }

        return true;
    }

    // Método para salvar uma imagem
    private String salvarImagem(Curso curso, MultipartFile file) throws IOException {
        // Verificar se o arquivo enviado é uma imagem JPG ou PNG
        validarImagem(file);

        // Salvar a foto no diretório
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("O arquivo enviado não possui um nome válido.");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String uniqueFileName = curso.getId() + "/" + UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
        Files.createDirectories(targetLocation.getParent());
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return this.baseStorageLocation + "/" + uniqueFileName;
    }

    // Método para excluir uma imagem
    private Boolean excluirImagem(String fileName) {
        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(targetLocation);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
