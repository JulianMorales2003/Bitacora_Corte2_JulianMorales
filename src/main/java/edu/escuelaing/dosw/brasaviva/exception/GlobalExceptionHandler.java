package edu.escuelaing.dosw.brasaviva.exception;

import edu.escuelaing.dosw.brasaviva.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(" | "));
        log.warn("Validacion de input fallida: {}", errores);
        return ErrorResponseDTO.of(400, "Datos de entrada invalidos", errores, request.getRequestURI());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleValidacionParametros(HandlerMethodValidationException ex, HttpServletRequest request) {
        String errores = ex.getAllValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.joining(" | "));
        log.warn("Parametros invalidos: {}", errores);
        return ErrorResponseDTO.of(400, "Parametros invalidos", errores, request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleBodyIlegible(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Body ilegible en {}: {}", request.getRequestURI(), ex.getMessage());
        return ErrorResponseDTO.of(400, "Cuerpo de la peticion invalido",
                "El JSON enviado esta vacio o mal formado", request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleTipoIncorrecto(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String mensaje = "El parametro '" + ex.getName() + "' tiene un valor invalido: " + ex.getValue();
        log.warn(mensaje);
        return ErrorResponseDTO.of(400, "Parametro invalido", mensaje, request.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleParametroFaltante(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String mensaje = "Falta el parametro obligatorio '" + ex.getParameterName() + "'";
        log.warn(mensaje);
        return ErrorResponseDTO.of(400, "Parametro faltante", mensaje, request.getRequestURI());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleSolicitudInvalida(SolicitudInvalidaException ex, HttpServletRequest request) {
        log.warn("Solicitud invalida: {}", ex.getMessage());
        return ErrorResponseDTO.of(400, "Solicitud invalida", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ErrorResponseDTO.of(404, "No encontrado", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleRutaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("Ruta inexistente: {}", request.getRequestURI());
        return ErrorResponseDTO.of(404, "No encontrado", "La ruta solicitada no existe", request.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponseDTO handleMetodoNoSoportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Metodo {} no soportado en {}", ex.getMethod(), request.getRequestURI());
        return ErrorResponseDTO.of(405, "Metodo no permitido",
                "El metodo " + ex.getMethod() + " no esta soportado en esta ruta", request.getRequestURI());
    }

    @ExceptionHandler(ConflictoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO handleConflicto(ConflictoException ex, HttpServletRequest request) {
        log.warn("Conflicto de negocio: {}", ex.getMessage());
        return ErrorResponseDTO.of(409, "Conflicto", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponseDTO handleReglaNegocio(ReglaNegocioException ex, HttpServletRequest request) {
        log.warn("Regla de negocio violada: {}", ex.getMessage());
        return ErrorResponseDTO.of(422, "Regla de negocio", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        return ErrorResponseDTO.of(500, "Error interno", "Algo salio mal, intenta de nuevo", request.getRequestURI());
    }
}
