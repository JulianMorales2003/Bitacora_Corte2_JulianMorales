package edu.escuelaing.dosw.brasaviva.exception;

public class UsuarioYaExisteException extends ConflictoException {
    public UsuarioYaExisteException(String mensaje) {
        super(mensaje);
    }
}
