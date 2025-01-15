package edu.uea.acadmanage.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.uea.acadmanage.DTO.AtividadeDTO;
import edu.uea.acadmanage.DTO.AtividadeFiltroDTO;
import edu.uea.acadmanage.config.FileStorageProperties;
import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.Categoria;
import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.FonteFinanciadora;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.AtividadeRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoService cursoService;
    private final CategoriaService categoriaService;
    private final Path fileStorageLocation;
    private final String baseStorageLocation;

    public AtividadeService(
            AtividadeRepository atividadeRepository,
            UsuarioRepository usuarioRepository,
            CursoService cursoService,
            CategoriaService categoriaService,
            FileStorageProperties fileStorageProperties) throws IOException {
        this.atividadeRepository = atividadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoService = cursoService;
        this.categoriaService = categoriaService;
        this.baseStorageLocation = "/fotos-capa";
        this.fileStorageLocation = Paths.get(fileStorageProperties.getStorageLocation() + this.baseStorageLocation)
                .toAbsolutePath().normalize();
        Files.createDirectories(this.fileStorageLocation);
    }

    public List<AtividadeDTO> getAtividadesPorCurso(Long cursoId) {
        // Verificar se o curso existe
        if (!cursoService.verificarSeCursoExiste(cursoId)) {
            throw new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + cursoId);
        }

        // Buscar atividades associadas ao curso
        List<Atividade> atividades = atividadeRepository.findByCursoId(cursoId);

        // Converter atividades para DTO
        return atividades.stream()
                .map(this::toAtividadeDTO)
                .collect(Collectors.toList());
    }

    // Método para buscar uma atividade por ID e usuário
    public AtividadeDTO getAtividadeByIdAndUsuario(Long atividadeId, Long usuarioId) {
        // Obter cursos permitidos para o usuário
        List<Long> cursoIds = getCursoIdsByUsuario(usuarioId);
        // Se o usuário não tem cursos associados, retorna vazio
        if (cursoIds.isEmpty()) {
            throw new AcessoNegadoException("O usuário não tem permissão para acessar esta atividade.");
        }
        // Buscar a atividade restrita aos cursos permitidos
        return atividadeRepository.findByIdAndCursoIds(atividadeId, cursoIds)
                .map(this::toAtividadeDTO)
                .orElseThrow(
                        () -> new AcessoNegadoException("O usuário não tem permissão para acessar esta atividade."));
    }

    // Método para buscar uma atividade por ID
    public AtividadeDTO getAtividadeById(Long atividadeId) {
        return atividadeRepository.findById(atividadeId)
                .map(atividade -> toAtividadeDTO(atividade))
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Atividade não encontrada com o ID: " + atividadeId));
    }

    // Método para pesquisar atividades por filtros
    public List<AtividadeDTO> getAtividadesPorFiltros(AtividadeFiltroDTO filtro) {
        // Verificar se o curso existe
        if (!categoriaService.verificarSeCategoriaExiste(filtro.cursoId())) {
            throw new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + filtro.cursoId());
        }
        // Buscar atividades usando filtros no repositório
        List<Atividade> atividades = atividadeRepository.findByFiltros(
                filtro.cursoId(), filtro.categoriaId(), filtro.nome(), filtro.dataInicio(), filtro.dataFim(),
                filtro.statusPublicacao());

        // Converter entidades em DTOs
        return atividades.stream()
                .map(this::toAtividadeDTO)
                .collect(Collectors.toList());
    }

    // Método para salvar uma atividade
    public AtividadeDTO salvarAtividade(AtividadeDTO atividadeDTO, String username) {
        // Verificar se o curso existe
        if (!cursoService.verificarSeCursoExiste(atividadeDTO.curso().getId())) {
            throw new RecursoNaoEncontradoException(
                    "Curso não encontrado com o ID: " + atividadeDTO.curso().getId());
        }

        // Verificar se a categoria existe
        if (!categoriaService.verificarSeCategoriaExiste(atividadeDTO.categoria().getId())) {
            throw new RecursoNaoEncontradoException(
                    "Categoria não encontrada com o ID: " + atividadeDTO.categoria().getId());
        }


        // Verificar se o usuário tem permissão para salvar a atividade
        if (!cursoService.verificarAcessoAoCurso(username, atividadeDTO.categoria().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para salvar atividade no curso: " + atividadeDTO.categoria().getId());
        }

        // Criar a entidade Atividade
        Atividade novaAtividade = toAtividade(atividadeDTO);
        novaAtividade.setFotoCapa(null);

        // Salvar no banco
        Atividade atividadeSalva = atividadeRepository.save(novaAtividade);

        // Retornar o DTO da atividade salva
        return toAtividadeDTO(atividadeSalva);
    }


    // Método para atualizar uma foto de capa
    public AtividadeDTO atualizarFotoCapa(Long atividadeId,
            MultipartFile file,
            String username) throws IOException {

        // Verificar se a atividade associada existe
        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Atividade não encontrada com o ID: " + atividadeId));

        // Verificar se o usuário tem permissão para salvar a evidência
        if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para salvar a evidência no curso: " + atividade.getCurso().getId());
        }

        if (file != null) {
            if (atividade.getFotoCapa() != null) {
                if (!excluirImagem(atividade.getFotoCapa())) {
                    throw new IllegalArgumentException("O arquivo anterior não pode ser removido.");
                }
            }           
            atividade.setFotoCapa(salvarImagem(atividade, file));
        }

        // Atualiza atividade com capa
        Atividade atividadeAtualizada = atividadeRepository.save(atividade);

        // Retornar o DTO da evidência atualizada
        return toAtividadeDTO(atividadeAtualizada);
    }

    // Método para excluir uma foto de capa
    public void excluirFotoCapa(Long atividadeId, String username) {
        // Verificar se a atividade associada existe
        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Atividade não encontrada com o ID: " + atividadeId));
        // Verificar se o usuário tem permissão para excluir a imagem
        if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para excluir a foto de capa da atividade com id: " + atividade.getCurso().getId());
        }
        // Excluir a foto de capa
        if (atividade.getFotoCapa() != null) {
            excluirImagem(atividade.getFotoCapa());
            atividade.setFotoCapa(null);
            atividadeRepository.save(atividade);
        }
    }

    // Método para atualizar uma atividade
    public AtividadeDTO atualizarAtividade(Long atividadeId, AtividadeDTO atividadeDTO, String username) {
        // Verificar se a atividade existe
        Atividade atividadeExistente = atividadeRepository.findById(atividadeId)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Atividade não encontrada com o ID: " + atividadeId));

        // Verificar se o curso existe
        if (!cursoService.verificarSeCursoExiste(atividadeDTO.curso().getId())) {
            throw new RecursoNaoEncontradoException(
                    "Curso não encontrado com o ID: " + atividadeDTO.curso().getId());
        }

        // Verificar se a categoria existe
        if (!categoriaService.verificarSeCategoriaExiste(atividadeDTO.categoria().getId())) {
            throw new RecursoNaoEncontradoException(
                    "Categoria não encontrada com o ID: " + atividadeDTO.categoria().getId());
        }

        // Verificar se o usuário tem permissão para salvar a atividade
        if (!cursoService.verificarAcessoAoCurso(username, atividadeDTO.curso().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para atualizar atividade no curso: " + atividadeDTO.curso().getId());
        }

        atividadeExistente.setNome(atividadeDTO.nome());
        atividadeExistente.setObjetivo(atividadeDTO.objetivo());
        atividadeExistente.setPublicoAlvo(atividadeDTO.publicoAlvo());
        atividadeExistente.setStatusPublicacao(atividadeDTO.statusPublicacao());
        atividadeExistente.setDataRealizacao(atividadeDTO.dataRealizacao());
        atividadeExistente.setCurso(atividadeDTO.curso());
        atividadeExistente.setCategoria(atividadeDTO.categoria());
        atividadeExistente.setFontesFinanciadora(atividadeDTO.fontesFinanciadora().stream()
                .map(fonte -> new FonteFinanciadora(fonte.getId()))
                .collect(Collectors.toList()));

        // Salvar no banco
        Atividade atividadeAtualizada = atividadeRepository.save(atividadeExistente);

        // Retornar o DTO da atividade atualizada
        return toAtividadeDTO(atividadeAtualizada);
    }

    // Método para excluir uma atividade
    public void excluirAtividade(Long atividadeId, String username) {
        // Verificar se a atividade existe
        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Atividade não encontrada com o ID: " + atividadeId));

        // Verificar se o usuário tem permissão para excluir a atividade
        if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para excluir atividade no curso: " + atividade.getCurso().getId());
        }

        // Excluir a foto de capa
        if (atividade.getFotoCapa() != null) {
            excluirImagem(atividade.getFotoCapa());
        }

        // Excluir a atividade
        atividadeRepository.deleteById(atividadeId);
    }

    // Método para buscar um curso por atividade
    public Curso buscarCursoPorAtividade(Long atividadeId) {
        return atividadeRepository.findById(atividadeId)
                .map(Atividade::getCurso) // Recupera o curso associado
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Atividade não encontrada com ID: " + atividadeId));
    }

    // Método privado para obter os IDs dos cursos de um usuário
    private List<Long> getCursoIdsByUsuario(Long usuarioId) {
        // Verificar se o usuário existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + usuarioId));
        return usuario.getCursos().stream()
                .map(Curso::getId)
                .toList();
    }

    // Método privado para converter Atividade para AtividadeDTO
    private AtividadeDTO toAtividadeDTO(Atividade atividade) {
        return new AtividadeDTO(
                atividade.getId(),
                atividade.getNome(),
                atividade.getObjetivo(),
                atividade.getPublicoAlvo(),
                atividade.getStatusPublicacao(),
                atividade.getFotoCapa(),
                atividade.getCoordenador(),
                atividade.getDataRealizacao(),
                atividade.getCurso(),
                atividade.getCategoria(),
                atividade.getFontesFinanciadora());
    }

    // Método privado para converter AtividadeDTO para Atividade
    private Atividade toAtividade(AtividadeDTO atividadeDTO) {
        Atividade atividade = new Atividade();
        atividade.setNome(atividadeDTO.nome());
        atividade.setObjetivo(atividadeDTO.objetivo());
        atividade.setPublicoAlvo(atividadeDTO.publicoAlvo());
        atividade.setStatusPublicacao(atividadeDTO.statusPublicacao());
        atividade.setDataRealizacao(atividadeDTO.dataRealizacao());
        atividade.setCurso(atividadeDTO.curso());
        atividade.setCategoria(atividadeDTO.categoria());
        atividade.setFontesFinanciadora(atividadeDTO.fontesFinanciadora().stream()
                .map(fonte -> new FonteFinanciadora(fonte.getId()))
                .collect(Collectors.toList()));
        return atividade;
    }

    private Boolean validarImagem(MultipartFile file) {
        // Verificar se o arquivo enviado é uma imagem JPG ou PNG
        Set<String> allowedContentTypes = Set.of("image/jpg", "image/jpeg", "image/png");
        if (!allowedContentTypes.contains(Objects.requireNonNullElse(file.getContentType(), "").toLowerCase())) {
            throw new IllegalArgumentException("O arquivo enviado deve ser um JPG, JPEG ou PNG válido.");
        }

        return true;
    }

    private String salvarImagem(Atividade atividade, MultipartFile file) throws IOException {
        // Verificar se o arquivo enviado é uma imagem JPG ou PNG
        validarImagem(file);

        // Salvar a foto no diretório
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("O arquivo enviado não possui um nome válido.");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String uniqueFileName = atividade.getCurso().getId() + "/" + atividade.getId() + "/"
                + UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
        Files.createDirectories(targetLocation);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return this.baseStorageLocation+"/"+uniqueFileName;
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