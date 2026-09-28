package edu.escuelaing.dosw.brasaviva.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- Excepciones de dominio ----------

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrado(RecursoNoEncontradoException ex,
                                                            HttpServletRequest req) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> handleConflicto(ConflictoException ex,
                                                         HttpServletRequest req) {
        log.warn("Conflicto: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> handleReglaNegocio(ReglaNegocioException ex,
                                                            HttpServletRequest req) {
        log.warn("Regla de negocio violada ({}): {}", ex.getClass().getSimpleName(), ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleSolicitudInvalida(SolicitudInvalidaException ex,
                                                                 HttpServletRequest req) {
        log.warn("Solicitud inválida: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, List.of());
    }

    // ---------- Validación de input (@Valid) ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException ex,
                                                              HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        log.warn("Validación fallida en {}: {}", req.getRequestURI(), details);
        return build(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", req, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex,
                                                          HttpServletRequest req) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Parámetros inválidos", req, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición es inválido o está mal formado", req, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un valor inválido", req, List.of());
    }

    // ---------- Errores de Spring MVC (evitan que caigan como 500) ----------

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex,
                                                          HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Ruta no encontrada", req, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                  HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido", req, List.of());
    }

    // ---------- Fallback ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado en {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req, List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                HttpServletRequest req, List<String> details) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), message, req.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }
}