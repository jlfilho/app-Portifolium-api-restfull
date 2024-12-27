package edu.uea.acadmanage.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.disable()) // Desabilita CSRF (caso necessário)
            // Desabilita proteção de frames para o H2 Console
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/logout","/api/atividades/**", "/api/cursos/**", "/api/categorias/**").permitAll() // Permite acesso a login e logout sem autenticação
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/h2-console/**").hasRole("ADMINISTRADOR")
                .anyRequest().authenticated() // Todas as outras requisições requerem autenticação
            )
            .httpBasic(Customizer.withDefaults()) // Habilita autenticação básica
            .formLogin(form -> form
                .loginPage("/login") // Página de login personalizada
                .defaultSuccessUrl("/swagger-ui/index.html", true) // Redireciona para o Swagger após login
                .permitAll() // Permite acesso à página de login para todos
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // URL de logout
                .logoutSuccessUrl("/login?logout") // Redireciona após logout
                .permitAll() // Permite logout para todos
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}