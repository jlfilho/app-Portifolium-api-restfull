package edu.uea.acadmanage.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
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
        private final List<String> allowedOrigins;
        private final boolean allowH2Console;

        public JwtSecurityConfig(
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        AuthenticationProvider authenticationProvider,
                        @Value("${app.cors.allowed-origins}") String allowedOrigins,
                        @Value("${app.security.allow-h2-console:false}") boolean allowH2Console) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.authenticationProvider = authenticationProvider;
                this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                                .map(String::trim)
                                .filter(origin -> !origin.isEmpty())
                                .toList();
                this.allowH2Console = allowH2Console;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita o suporte
                                                                                                   // a CORS
                                .csrf(csrf -> csrf.disable())
                                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                                .authorizeHttpRequests(auth -> {
                                        auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                                                        "/v3/api-docs.yaml", "/swagger-resources/**")
                                                        .permitAll();
                                        auth.requestMatchers("/api/auth/login/**", "/api/auth/logout/**", "/logout**")
                                                        .permitAll();
                                        auth.requestMatchers("/api/recovery/generate/**",
                                                        "/api/recovery/reset-password/**")
                                                        .permitAll();
                                        auth.requestMatchers("/css/**", "/js/**", "/images/**", "/api/files/**")
                                                        .permitAll();
                                        auth.requestMatchers(HttpMethod.GET,
                                                        "/api/atividades/**",
                                                        "/api/cursos/**",
                                                        "/api/categorias/**",
                                                        "/api/evidencias/**",
                                                        "/api/unidades-academicas/**")
                                                        .permitAll();
                                        // Endpoints minimos do Actuator para healthcheck e monitoramento
                                        auth.requestMatchers("/actuator/health", "/actuator/health/**",
                                                        "/actuator/info", "/actuator/prometheus")
                                                        .permitAll();
                                        auth.requestMatchers("/actuator/**")
                                                        .authenticated();
                                        if (allowH2Console) {
                                                auth.requestMatchers("/h2-console/**").permitAll();
                                        } else {
                                                auth.requestMatchers("/h2-console/**").denyAll();
                                        }
                                        auth.anyRequest().authenticated();
                                })
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public UrlBasedCorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(allowedOrigins);
                configuration.addAllowedMethod("*");
                configuration.addAllowedHeader("*");
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

}
