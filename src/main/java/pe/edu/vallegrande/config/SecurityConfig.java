package pe.edu.vallegrande.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)

            .authorizeExchange(auth -> auth
                // Pre‑flight CORS
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Swagger (público)
                .pathMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

                /* ----------  ENDPOINTS HEN  ---------- */

                // GET generales accesibles para USER y ADMIN
                .pathMatchers(HttpMethod.GET, "/hen", "/hen/{id}", "/hen/activos",
                                             "/hen/buscar/**").hasAnyRole("USER", "ADMIN")

                // GET de inactivos solo ADMIN (ej. /hen/inactivos)
                .pathMatchers(HttpMethod.GET, "/hen/inactivos/**").hasRole("ADMIN")

                // Mutaciones (POST, PUT, DELETE) solo ADMIN
                .pathMatchers(HttpMethod.POST,   "/hen").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT,    "/hen/update/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT,    "/hen/inactivar/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT,    "/hen/activar/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/hen/**").hasRole("ADMIN")

                // Cualquier otra ruta requiere estar autenticado
                .anyExchange().authenticated()
            )

            // Recurso protegido por JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtDecoder(jwtDecoder())
                    .jwtAuthenticationConverter(this::convertJwt)
                )
            )

            // CORS para el frontend
            .cors(cors -> cors.configurationSource(exchange -> {
                var cfg = new org.springframework.web.cors.CorsConfiguration();
                cfg.setAllowCredentials(true);
                cfg.addAllowedOrigin("http://localhost:4200");
                cfg.addAllowedHeader("*");
                cfg.addAllowedMethod("*");
                return cfg;
            }))

            .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /** Convierte el claim "role" del JWT en ROLE_USER / ROLE_ADMIN */
    private Mono<CustomAuthenticationToken> convertJwt(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        Collection<GrantedAuthority> auths = role != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                : List.of();
        return Mono.just(new CustomAuthenticationToken(jwt, auths));
    }
}
