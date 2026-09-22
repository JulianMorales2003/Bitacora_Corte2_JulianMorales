package edu.escuelaing.dosw.brasaviva.exception;

public abstract class RecursoNoEncontradoException extends RuntimeException {
    protected RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}