package edu.escuelaing.dosw.brasaviva.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dosw.brasaviva.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Escribe errores 401/403 con el mismo formato JSON que el resto de la API. Se necesita porque
 * estos errores ocurren dentro de los filtros, antes de llegar al GlobalExceptionHandler.
 */
final class SecurityErrorWriter {

    private SecurityErrorWriter() {
    }

    static void escribir(ObjectMapper mapper, HttpServletResponse response, HttpStatus status,
                         String mensaje, String path) throws IOException {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), mensaje, path, List.of());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), body);
    }
}
