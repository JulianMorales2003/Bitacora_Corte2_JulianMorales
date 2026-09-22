package edu.escuelaing.dosw.brasaviva.exception;

public abstract class ConflictoException extends RuntimeException {
    protected ConflictoException(String mensaje) {
        super(mensaje);
    }
}