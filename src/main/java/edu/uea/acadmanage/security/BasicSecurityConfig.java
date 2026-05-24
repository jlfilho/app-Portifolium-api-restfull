package edu.uea.acadmanage.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("basic")
@EnableMethodSecurity
public class BasicSecurityConfig {

    @Value("${app.security.allow-h2-console:false}")
    private boolean allowH2Console;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/login", "/logout").permitAll();
                auth.requestMatchers("/api/recovery/generate/**", "/api/recovery/reset-password/**").permitAll();
                auth.requestMatchers("/css/**", "/js/**", "/images/**", "/api/files/**").permitAll();
                auth.requestMatchers(HttpMethod.GET,
                    "/api/atividades/**",
                    "/api/cursos/**",
                    "/api/categorias/**",
                    "/api/evidencias/**",
                    "/api/unidades-academicas/**").permitAll();
                auth.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                auth.requestMatchers("/actuator/**").authenticated();
                if (allowH2Console) {
                    auth.requestMatchers("/h2-console/**").permitAll();
                } else {
                    auth.requestMatchers("/h2-console/**").denyAll();
                }
                auth.anyRequest().authenticated();
            })
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/swagger-ui/index.html", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
