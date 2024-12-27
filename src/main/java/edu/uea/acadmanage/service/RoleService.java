package edu.uea.acadmanage.service;

import org.springframework.stereotype.Service;

import edu.uea.acadmanage.model.Role;
import edu.uea.acadmanage.repository.RoleRepository;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Verifica se uma role é válida consultando o banco de dados.
     *
     * @param role a role a ser verificada
     * @return true se a role for válida, false caso contrário
     */
    public boolean isRoleValida(String role) {
        return roleRepository.existsByNome(role);
    }

    /**
     * Obtém uma role pelo nome.
     *
     * @param nome o nome da role
     * @return a role
     * @throws RecursoNaoEncontradoException se a role não for encontrada
     */
    public Role getRoleByNome(String nome) {
        return roleRepository.findByNome(nome)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Role não encontrada: " + nome));
    }

    /**
     * Obtém uma role pelo ID.
     *
     * @param id o ID da role
     * @return a role
     * @throws RecursoNaoEncontradoException se a role não for encontrada
     */
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Role não encontrada com o ID: " + id));
    }
}
