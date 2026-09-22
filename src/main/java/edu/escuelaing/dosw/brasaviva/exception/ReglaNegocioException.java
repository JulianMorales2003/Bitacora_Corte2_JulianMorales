package edu.escuelaing.dosw.brasaviva.exception;

public abstract class ReglaNegocioException extends RuntimeException {
    protected ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}