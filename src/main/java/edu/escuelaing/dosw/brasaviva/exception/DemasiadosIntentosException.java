package edu.escuelaing.dosw.brasaviva.exception;

/** Se supero el limite de intentos de login fallidos (proteccion contra fuerza bruta, OWASP A07). */
public class DemasiadosIntentosException extends RuntimeException {
    public DemasiadosIntentosException(String mensaje) {
        super(mensaje);
    }
}
