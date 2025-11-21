package edu.uea.acadmanage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    public static class SpringSecurityAuditorAware implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                
                if (authentication == null || 
                    !authentication.isAuthenticated() || 
                    "anonymousUser".equals(authentication.getPrincipal())) {
                    return Optional.of("system");
                }
                
                String username = authentication.getName();
                // Retorna o email do usuário autenticado, ou "system" se null
                return Optional.of(username != null && !username.isEmpty() ? username : "system");
            } catch (Exception e) {
                return Optional.of("system");
            }
        }
    }
}

