package edu.uea.acadmanage.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@Profile("jwt")
@EnableWebSecurity
@EnableMethodSecurity
public class JwtSecurityConfig {
        private final AuthenticationProvider authenticationProvider;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public JwtSecurityConfig(
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        AuthenticationProvider authenticationProvider) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.authenticationProvider = authenticationProvider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita o suporte
                                                                                                   // a CORS
                                .csrf(csrf -> csrf.disable())
                                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                                                                "/v3/api-docs.yaml", "/swagger-resources/**")
                                                .permitAll()
                                                .requestMatchers("/api/auth/login/**", "/logout**",
                                                                "/api/atividades/**", "/api/cursos/**",
                                                                "/api/categorias/**", "/api/evidencias/**",
                                                                "/api/evidencias/atividade/**",
                                                                "/api/categorias/curso/**")
                                                .permitAll()
                                                .requestMatchers("/api/recovery/generate/**",
                                                                "/api/recovery/reset-password/**")
                                                .permitAll()
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/api/files/**")
                                                .permitAll()
                                                .requestMatchers("/h2-console/**").hasRole("ADMINISTRADOR")
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public UrlBasedCorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.addAllowedOrigin("http://localhost:4200"); // URL do frontend
                configuration.addAllowedMethod("*");
                configuration.addAllowedHeader("*");
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

}
