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
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.uea.acadmanage.DTO.AtividadeDTO;
import edu.uea.acadmanage.DTO.AtividadeFiltroDTO;
import edu.uea.acadmanage.DTO.PessoaPapelDTO;
import edu.uea.acadmanage.config.FileStorageProperties;
import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.AtividadePessoaId;
import edu.uea.acadmanage.model.AtividadePessoaPapel;
import edu.uea.acadmanage.model.Curso;
import edu.uea.acadmanage.model.FonteFinanciadora;
import edu.uea.acadmanage.model.Papel;
import edu.uea.acadmanage.model.Pessoa;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.AtividadeRepository;
import edu.uea.acadmanage.repository.PessoaRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import edu.uea.acadmanage.service.exception.ArquivoInvalidoException;
import edu.uea.acadmanage.service.exception.AtividadeComEvidenciasException;
import edu.uea.acadmanage.service.exception.ErroProcessamentoArquivoException;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoService cursoService;
    private final CategoriaService categoriaService;
    private final FonteFinanciadoraService fonteFinanciadoraService;
    private final PessoaRepository pessoaRepository;
    private final AtividadeAutorizacaoService atividadeAutorizacaoService;
    private final Path fileStorageLocation;
    private final String baseStorageLocation;

    public AtividadeService(
            AtividadeRepository atividadeRepository,
            UsuarioRepository usuarioRepository,
            CursoService cursoService,
            CategoriaService categoriaService,
            FonteFinanciadoraService fonteFinanciadoraService,
            PessoaRepository pessoaRepository,
            AtividadeAutorizacaoService atividadeAutorizacaoService,
            FileStorageProperties fileStorageProperties) throws IOException {
        this.atividadeRepository = atividadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoService = cursoService;
        this.categoriaService = categoriaService;
        this.fonteFinanciadoraService = fonteFinanciadoraService;
        this.pessoaRepository = pessoaRepository;
        this.atividadeAutorizacaoService = atividadeAutorizacaoService;
        this.baseStorageLocation = "fotos-capa";
        this.fileStorageLocation = Paths.get(fileStorageProperties.getStorageLocation())
                .resolve(this.baseStorageLocation)
                .toAbsolutePath()
                .normalize();
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
        // Validar se o curso existe (apenas se cursoId foi fornecido)
        if (filtro.cursoId() != null && !cursoService.verificarSeCursoExiste(filtro.cursoId())) {
            throw new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + filtro.cursoId());
        }
        
        // Validar se a categoria existe (apenas se categoriaId foi fornecido)
        if (filtro.categoriaId() != null && !categoriaService.verificarSeCategoriaExiste(filtro.categoriaId())) {
            throw new RecursoNaoEncontradoException("Categoria não encontrada com o ID: " + filtro.categoriaId());
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

    // Método para pesquisar atividades por filtros com paginação
    public Page<AtividadeDTO> getAtividadesPorFiltrosPaginado(AtividadeFiltroDTO filtro, Pageable pageable) {
        // Validar se o curso existe (apenas se cursoId foi fornecido)
        if (filtro.cursoId() != null && !cursoService.verificarSeCursoExiste(filtro.cursoId())) {
            throw new RecursoNaoEncontradoException("Curso não encontrado com o ID: " + filtro.cursoId());
        }
        
        // Validar se a categoria existe (apenas se categoriaId foi fornecido)
        if (filtro.categoriaId() != null && !categoriaService.verificarSeCategoriaExiste(filtro.categoriaId())) {
            throw new RecursoNaoEncontradoException("Categoria não encontrada com o ID: " + filtro.categoriaId());
        }
        
        // Buscar atividades usando filtros no repositório com paginação
        Page<Atividade> atividades = atividadeRepository.findByFiltrosPaginado(
                filtro.cursoId(), filtro.categoriaId(), filtro.nome(), filtro.dataInicio(), filtro.dataFim(),
                filtro.statusPublicacao(), pageable);

        // Converter entidades em DTOs
        return atividades.map(this::toAtividadeDTO);
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


        // Verificar se o usuário tem permissão para criar a atividade
        Long cursoId = atividadeDTO.curso().getId();
        if (!atividadeAutorizacaoService.podeCriarAtividadeNoCurso(username, cursoId)) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para criar atividade no curso: " + cursoId);
        }

        // Criar a entidade Atividade
        Atividade novaAtividade = toAtividade(atividadeDTO);
        novaAtividade.setFotoCapa(null);

        // Salvar no banco primeiro para gerar o ID
        Atividade atividadeSalva = atividadeRepository.save(novaAtividade);

        // Sincronizar integrantes (pessoas) da atividade (precisa do ID gerado)
        sincronizarIntegrantes(atividadeSalva, atividadeDTO.integrantes());

        // Salvar novamente com os integrantes
        atividadeSalva = atividadeRepository.save(atividadeSalva);

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

        // Verificar se o usuário tem permissão para editar a atividade
        if (!atividadeAutorizacaoService.podeEditarAtividade(username, atividadeId)) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para editar esta atividade: " + atividadeId);
        }

        if (file != null) {
            if (atividade.getFotoCapa() != null) {
                if (!excluirImagem(atividade.getFotoCapa())) {
                    throw new ErroProcessamentoArquivoException("O arquivo anterior não pode ser removido.");
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
        // Verificar se o usuário tem permissão para editar a atividade
        if (!atividadeAutorizacaoService.podeEditarAtividade(username, atividadeId)) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para editar esta atividade: " + atividadeId);
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

        // Verificar se o usuário tem permissão para editar a atividade
        if (!atividadeAutorizacaoService.podeEditarAtividade(username, atividadeId)) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para editar esta atividade: " + atividadeId);
        }

        atividadeExistente.setNome(atividadeDTO.nome());
        atividadeExistente.setObjetivo(atividadeDTO.objetivo());
        atividadeExistente.setPublicoAlvo(atividadeDTO.publicoAlvo());
        atividadeExistente.setStatusPublicacao(atividadeDTO.statusPublicacao());
        atividadeExistente.setDataRealizacao(atividadeDTO.dataRealizacao());
        atividadeExistente.setDataFim(atividadeDTO.dataFim());
        atividadeExistente.setCurso(atividadeDTO.curso());
        atividadeExistente.setCategoria(atividadeDTO.categoria());
        
        // Buscar fontes financiadoras do banco para associá-las corretamente (entidades gerenciadas)
        if (atividadeDTO.fontesFinanciadora() != null && !atividadeDTO.fontesFinanciadora().isEmpty()) {
            List<FonteFinanciadora> fontesGerenciadas = atividadeDTO.fontesFinanciadora().stream()
                    .map(fonte -> fonteFinanciadoraService.recuperarFinanciadoraPorId(fonte.getId()))
                    .collect(Collectors.toList());
            atividadeExistente.setFontesFinanciadora(fontesGerenciadas);
        } else {
            atividadeExistente.setFontesFinanciadora(new ArrayList<>());
        }

        // Sincronizar integrantes (pessoas) da atividade
        sincronizarIntegrantes(atividadeExistente, atividadeDTO.integrantes());

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
        if (!atividadeAutorizacaoService.podeEditarAtividade(username, atividadeId)) {
            throw new AcessoNegadoException(
                    "Usuário não tem permissão para excluir esta atividade: " + atividadeId);
        }

        // Impedir exclusão quando existirem evidências cadastradas
        if (atividade.getEvidencias() != null && !atividade.getEvidencias().isEmpty()) {
            throw new AtividadeComEvidenciasException();
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
                atividade.getDataFim(),
                atividade.getCurso(),
                atividade.getCategoria(),
                atividade.getFontesFinanciadora(),
                atividade.getPessoas().stream()
                        .map(pessoa -> new PessoaPapelDTO(
                                pessoa.getPessoa().getId(),
                                pessoa.getPessoa().getNome(),
                                pessoa.getPessoa().getCpf(),
                                pessoa.getPapel().name()))
                        .toList());
    }

    // Método privado para converter AtividadeDTO para Atividade
    private Atividade toAtividade(AtividadeDTO atividadeDTO) {
        Atividade atividade = new Atividade();
        atividade.setNome(atividadeDTO.nome());
        atividade.setObjetivo(atividadeDTO.objetivo());
        atividade.setPublicoAlvo(atividadeDTO.publicoAlvo());
        atividade.setStatusPublicacao(atividadeDTO.statusPublicacao());
        atividade.setDataRealizacao(atividadeDTO.dataRealizacao());
        atividade.setDataFim(atividadeDTO.dataFim());
        atividade.setCurso(atividadeDTO.curso());
        atividade.setCategoria(atividadeDTO.categoria());
        
        // Buscar fontes financiadoras do banco para associá-las corretamente (entidades gerenciadas)
        if (atividadeDTO.fontesFinanciadora() != null && !atividadeDTO.fontesFinanciadora().isEmpty()) {
            List<FonteFinanciadora> fontesGerenciadas = atividadeDTO.fontesFinanciadora().stream()
                    .map(fonte -> fonteFinanciadoraService.recuperarFinanciadoraPorId(fonte.getId()))
                    .collect(Collectors.toList());
            atividade.setFontesFinanciadora(fontesGerenciadas);
        } else {
            atividade.setFontesFinanciadora(new ArrayList<>());
        }
        
        return atividade;
    }

    // Método privado para sincronizar integrantes (adicionar/remover/atualizar)
    private void sincronizarIntegrantes(Atividade atividade, List<PessoaPapelDTO> integrantesDTO) {
        if (integrantesDTO == null) {
            // Se não foi fornecida lista de integrantes, não faz nada (mantém os existentes)
            return;
        }

        // Obter lista atual de pessoas na atividade
        List<AtividadePessoaPapel> pessoasExistentes = atividade.getPessoas();
        
        // Limpar a lista existente (orphanRemoval cuidará da remoção no banco)
        pessoasExistentes.clear();
        
        // Adicionar os novos integrantes
        for (PessoaPapelDTO integranteDTO : integrantesDTO) {
            // Buscar a pessoa no banco
            Pessoa pessoa = pessoaRepository.findById(integranteDTO.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Pessoa não encontrada com o ID: " + integranteDTO.id()));
            
            // Criar a associação AtividadePessoaPapel
            AtividadePessoaId id = new AtividadePessoaId(atividade.getId(), pessoa.getId());
            AtividadePessoaPapel associacao = new AtividadePessoaPapel();
            associacao.setId(id);
            associacao.setAtividade(atividade);
            associacao.setPessoa(pessoa);
            associacao.setPapel(Papel.valueOf(integranteDTO.papel()));
            
            // Adicionar à lista
            pessoasExistentes.add(associacao);
        }
    }

    private Boolean validarImagem(MultipartFile file) {
        // Verificar se o arquivo enviado é uma imagem JPG ou PNG
        Set<String> allowedContentTypes = Set.of("image/jpg", "image/jpeg", "image/png");
        if (!allowedContentTypes.contains(Objects.requireNonNullElse(file.getContentType(), "").toLowerCase())) {
            throw new ArquivoInvalidoException("O arquivo enviado deve ser um JPG, JPEG ou PNG válido.");
        }

        return true;
    }

    private String salvarImagem(Atividade atividade, MultipartFile file) throws IOException {
        // Verificar se o arquivo enviado é uma imagem JPG ou PNG
        validarImagem(file);

        // Salvar a foto no diretório
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new ArquivoInvalidoException("O arquivo enviado não possui um nome válido.");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String uniqueFileName = atividade.getCurso().getId() + "/" + atividade.getId() + "/"
                + UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName).normalize();
        Files.createDirectories(targetLocation.getParent());
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return "/" + this.baseStorageLocation + "/" + uniqueFileName;
    }

    // Método para excluir uma imagem
    private Boolean excluirImagem(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }

        try {
            Path candidatePath = Paths.get(fileName).normalize();

            if (candidatePath.isAbsolute() && candidatePath.startsWith(this.fileStorageLocation)) {
                Files.deleteIfExists(candidatePath);
                return true;
            }

            String normalized = fileName.replace("\\", "/");

            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }

            String basePrefix = this.baseStorageLocation.replace("\\", "/");
            if (normalized.startsWith(basePrefix + "/")) {
                normalized = normalized.substring(basePrefix.length() + 1);
            } else if (normalized.equals(basePrefix)) {
                normalized = "";
            }

            Path targetLocation = this.fileStorageLocation.resolve(normalized).normalize();
            Files.deleteIfExists(targetLocation);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}