package edu.escuelaing.dosw.brasaviva.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Responde 401 cuando no hay token, es invalido o esta vencido. */
@Component
@Slf4j
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        log.warn("401 No autenticado: {} {}", request.getMethod(), request.getRequestURI());
        SecurityErrorWriter.escribir(objectMapper, response, HttpStatus.UNAUTHORIZED,
                "Autenticacion requerida: token ausente, invalido o vencido", request.getRequestURI());
    }
}
