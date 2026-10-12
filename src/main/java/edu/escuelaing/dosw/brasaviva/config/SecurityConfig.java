package edu.escuelaing.dosw.brasaviva.config;

import edu.escuelaing.dosw.brasaviva.security.JwtAuthFilter;
import edu.escuelaing.dosw.brasaviva.security.JwtUtil;
import edu.escuelaing.dosw.brasaviva.security.OAuth2SuccessHandler;
import edu.escuelaing.dosw.brasaviva.security.RestAccessDeniedHandler;
import edu.escuelaing.dosw.brasaviva.security.RestAuthenticationEntryPoint;
import edu.escuelaing.dosw.brasaviva.security.UsuarioDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Cadena de seguridad de la API: JWT stateless + autorizacion por rol segun la matriz del README.
 * Cada regla de {@code authorizeHttpRequests} corresponde a una fila de la tabla
 * "Roles y permisos por endpoint" del README.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String CLIENTE = "CLIENTE";
    private static final String MESERO = "MESERO";
    private static final String PARRILLERO = "PARRILLERO";
    private static final String ADMIN = "ADMIN";

    /** Swagger UI necesita estilos inline; el resto queda restringido al propio origen. */
    private static final String CSP = "default-src 'self'; script-src 'self'; "
            + "style-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none'";

    private final JwtUtil jwtUtil;
    private final UsuarioDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final CorsConfig corsConfig;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    /** Solo existe si hay credenciales de Google configuradas (perfil {@code oauth2}). */
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrations;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API stateless con JWT en header: no hay cookie de sesion que un atacante pueda reutilizar
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                        .frameOptions(frame -> frame.deny())
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // ===== Sin autenticacion =====
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/menu/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()

                        // ===== Carta: ver (personal), marcar agotado (cocina/admin), editar (admin) =====
                        .requestMatchers(HttpMethod.GET, "/api/v1/platos/**").hasAnyRole(MESERO, PARRILLERO, ADMIN)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/platos/*/disponible").hasAnyRole(PARRILLERO, ADMIN)
                        .requestMatchers("/api/v1/platos/**").hasRole(ADMIN) // POST, PUT, DELETE

                        // ===== Pedidos y cocina =====
                        .requestMatchers(HttpMethod.POST, "/api/v1/pedidos", "/api/v1/pedidos/*/items")
                        .hasAnyRole(CLIENTE, MESERO)
                        .requestMatchers(HttpMethod.GET, "/api/v1/pedidos", "/api/v1/pedidos/cocina", "/api/v1/pedidos/mesa/*")
                        .hasAnyRole(MESERO, PARRILLERO, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/*")
                        .hasAnyRole(CLIENTE, MESERO, PARRILLERO, ADMIN)
                        // El estado destino define quien puede: ver PermisosPedido y el @PreAuthorize del controlador
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/*/estado")
                        .hasAnyRole(MESERO, PARRILLERO, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/pedidos/*").hasAnyRole(MESERO, ADMIN)

                        // ===== Salon: mesas, cuentas, reservas, parqueadero =====
                        .requestMatchers(HttpMethod.GET, "/api/v1/mesas/**").hasAnyRole(MESERO, PARRILLERO, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/mesas").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/cuentas", "/api/v1/cuentas/*/pago").hasRole(MESERO)
                        .requestMatchers(HttpMethod.GET, "/api/v1/cuentas/**").hasAnyRole(MESERO, ADMIN)
                        .requestMatchers("/api/v1/reservas/**").hasAnyRole(CLIENTE, MESERO, ADMIN)
                        .requestMatchers("/api/v1/parqueadero/**").hasAnyRole(MESERO, ADMIN)

                        // ===== Reportes y usuarios: solo administrador =====
                        .requestMatchers("/api/v1/reportes/**").hasRole(ADMIN)
                        .requestMatchers("/api/v1/usuarios/**").hasRole(ADMIN)

                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthFilter(jwtUtil, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        if (clientRegistrations.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2.successHandler(oAuth2SuccessHandler));
        }
        return http.build();
    }
}
