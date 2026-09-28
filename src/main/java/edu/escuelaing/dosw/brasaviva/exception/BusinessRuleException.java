package edu.escuelaing.dosw.brasaviva.exception;

/** Violación de una regla de negocio (RN-01, RN-02, RN-P02, ...). */
public class BusinessRuleException extends RuntimeException {
    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}