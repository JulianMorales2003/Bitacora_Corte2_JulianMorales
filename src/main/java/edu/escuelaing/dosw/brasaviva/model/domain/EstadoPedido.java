package edu.escuelaing.dosw.brasaviva.model.domain;

public enum EstadoPedido {
    RECIBIDO,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO;

    public boolean puedeTransicionarA(EstadoPedido destino) {
        return switch (this) {
            case RECIBIDO -> destino == EN_PREPARACION || destino == CANCELADO;
            case EN_PREPARACION -> destino == LISTO;
            case LISTO -> destino == ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }
}