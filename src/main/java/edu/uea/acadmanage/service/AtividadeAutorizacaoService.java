package edu.uea.acadmanage.service;

import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.Papel;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.AtividadePessoaPapelRepository;
import edu.uea.acadmanage.repository.AtividadeRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AtividadeAutorizacaoService {
    
    private final AtividadePessoaPapelRepository atividadePessoaPapelRepository;
    private final AtividadeRepository atividadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoService cursoService;
    
    public AtividadeAutorizacaoService(
            AtividadePessoaPapelRepository atividadePessoaPapelRepository,
            AtividadeRepository atividadeRepository,
            UsuarioRepository usuarioRepository,
            CursoService cursoService) {
        this.atividadePessoaPapelRepository = atividadePessoaPapelRepository;
        this.atividadeRepository = atividadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoService = cursoService;
    }
    
    /**
     * Verifica se o usuário tem permissão para criar atividades em um curso.
     * Permite: ADMINISTRADOR, GERENTE, SECRETARIO, COORDENADOR_ATIVIDADE (com acesso ao curso)
     * 
     * IMPORTANTE: ADMINISTRADOR, GERENTE e SECRETÁRIO precisam estar associados ao curso
     * (mantém comportamento atual)
     */
    public boolean podeCriarAtividadeNoCurso(String username, Long cursoId) {
        Usuario usuario = usuarioRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        
        // Verificar roles de sistema
        boolean isAdmin = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_ADMINISTRADOR"));
        boolean isGerente = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_GERENTE"));
        boolean isSecretario = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_SECRETARIO"));
        boolean isCoordenadorAtividade = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_COORDENADOR_ATIVIDADE"));
        
        // Admin, Gerente e Secretário precisam estar associados ao curso (comportamento atual)
        if (isAdmin || isGerente || isSecretario) {
            return cursoService.verificarAcessoAoCurso(username, cursoId);
        }
        
        // Coordenador de Atividade precisa ter acesso ao curso
        if (isCoordenadorAtividade) {
            return cursoService.verificarAcessoAoCurso(username, cursoId);
        }
        
        return false;
    }
    
    /**
     * Verifica se o usuário é coordenador de uma atividade específica.
     * Busca na tabela AtividadePessoaPapel se o usuário tem Papel.COORDENADOR na atividade.
     */
    public boolean ehCoordenadorDaAtividade(String username, Long atividadeId) {
        Atividade atividade = atividadeRepository.findById(atividadeId)
            .orElseThrow(() -> new AcessoNegadoException("Atividade não encontrada"));
        
        // Buscar usuário e pessoa associada
        Usuario usuario = usuarioRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        
        if (usuario.getPessoa() == null) {
            return false;
        }
        
        // Verificar se existe associação com Papel.COORDENADOR
        return atividadePessoaPapelRepository.existsByAtividadeAndPessoaAndPapel(
            atividade, 
            usuario.getPessoa(), 
            Papel.COORDENADOR
        );
    }
    
    /**
     * Verifica se o usuário pode editar uma atividade.
     * Permite: ADMINISTRADOR, GERENTE, SECRETARIO (se associados ao curso),
     * ou COORDENADOR_ATIVIDADE (se for coordenador da atividade).
     */
    public boolean podeEditarAtividade(String username, Long atividadeId) {
        Atividade atividade = atividadeRepository.findById(atividadeId)
            .orElseThrow(() -> new AcessoNegadoException("Atividade não encontrada"));
        
        Usuario usuario = usuarioRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        
        // Verificar roles de sistema
        boolean isAdmin = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_ADMINISTRADOR"));
        boolean isGerente = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_GERENTE"));
        boolean isSecretario = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_SECRETARIO"));
        
        // Admin, Gerente e Secretário precisam estar associados ao curso (comportamento atual)
        if (isAdmin || isGerente || isSecretario) {
            return cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId());
        }
        
        // Coordenador de Atividade só pode editar se for coordenador desta atividade específica
        boolean isCoordenadorAtividade = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_COORDENADOR_ATIVIDADE"));
        
        if (isCoordenadorAtividade) {
            return ehCoordenadorDaAtividade(username, atividadeId);
        }
        
        return false;
    }
    
    /**
     * Verifica se o usuário pode gerenciar evidências de uma atividade.
     * Mesma lógica de podeEditarAtividade.
     */
    public boolean podeGerenciarEvidencias(String username, Long atividadeId) {
        return podeEditarAtividade(username, atividadeId);
    }
}

