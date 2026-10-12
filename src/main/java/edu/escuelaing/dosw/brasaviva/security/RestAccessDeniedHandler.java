package edu.escuelaing.dosw.brasaviva.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Responde 403 cuando el usuario esta autenticado pero su rol no tiene acceso. */
@Component
@Slf4j
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.warn("403 Acceso denegado a {} {} (usuario: {})", request.getMethod(), request.getRequestURI(),
                auth == null ? "desconocido" : auth.getName());
        SecurityErrorWriter.escribir(objectMapper, response, HttpStatus.FORBIDDEN,
                "No tienes permisos para realizar esta accion", request.getRequestURI());
    }
}
