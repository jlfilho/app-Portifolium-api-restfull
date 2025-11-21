package edu.uea.acadmanage.controller;

import edu.uea.acadmanage.DTO.DashboardDTO;
import edu.uea.acadmanage.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Retorna dados consolidados para o dashboard do sistema.
     * Administradores visualizam números globais.
     * Gerentes, Secretários e Coordenadores visualizam apenas cursos associados.
     * 
     * @param userDetails Usuário logado
     * @return DashboardDTO com métricas gerais, atividades por categoria, 
     *         status de publicação, distribuição de usuários, cursos em destaque,
     *         atividades recentes e metas de progresso.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO') or hasRole('COORDENADOR_ATIVIDADE')")
    public ResponseEntity<DashboardDTO> obterDadosDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        if (username == null) {
            // Se não houver usuário autenticado, retornar dashboard vazio
            return ResponseEntity.ok(dashboardService.obterDadosDashboard(null));
        }
        DashboardDTO dashboard = dashboardService.obterDadosDashboard(username);
        return ResponseEntity.ok(dashboard);
    }
}

