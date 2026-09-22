package edu.escuelaing.dosw.brasaviva.exception;

public abstract class SolicitudInvalidaException extends RuntimeException {
    protected SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}