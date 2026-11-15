package br.com.fiap.siase.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * JWT HS256 compartilhado com a auth-lambda da Fase 3 (mesmo JWT_SECRET).
 * Endpoints de acompanhamento/aprovação por número da OS são públicos (cliente).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")).build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.requireCsrfProtectionMatcher(request -> {
                    String uri = request.getRequestURI();
                    String authorization = request.getHeader("Authorization");
                    boolean bearerToken = authorization != null && authorization.startsWith("Bearer ");
                    boolean endpointPublico = uri.startsWith("/api/ordens/acompanhar/");

                    return !bearerToken && !endpointPublico;
                }));
        return http.build();
    }
}
