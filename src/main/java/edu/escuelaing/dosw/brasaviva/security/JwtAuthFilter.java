package edu.escuelaing.dosw.brasaviva.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepta cada peticion, valida el header {@code Authorization: Bearer <jwt>} y, si es valido,
 * deja al usuario autenticado en el contexto de seguridad. Si el token falta o es invalido no
 * autentica nada: la regla de autorizacion del endpoint responde 401.
 *
 * <p>No es un {@code @Component} a proposito: se instancia en {@code SecurityConfig} para que
 * solo se ejecute dentro de la cadena de Spring Security (y no dos veces).
 */
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UsuarioDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UsuarioDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            autenticar(header.substring(PREFIJO.length()), request);
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String token, HttpServletRequest request) {
        try {
            Claims claims = jwtUtil.extractClaims(token);
            // Se recarga el usuario de la BD: los cambios de rol o usuarios eliminados aplican de inmediato
            UserDetails user = userDetailsService.loadUserByUsername(claims.getSubject());
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException | IllegalArgumentException e) {
            // OWASP A09: se registran los tokens invalidos o vencidos
            log.warn("Token JWT rechazado en {} {}: {}", request.getMethod(), request.getRequestURI(),
                    e.getClass().getSimpleName());
        } catch (UsernameNotFoundException e) {
            log.warn("Token JWT valido de un usuario que ya no existe en {}", request.getRequestURI());
        }
    }
}