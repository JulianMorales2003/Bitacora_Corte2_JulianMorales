package edu.escuelaing.dosw.brasaviva.model.domain;

import java.time.LocalDateTime;

public class Cuenta {
    private Long id;
    private Long idMesa;
    private Double total;
    private EstadoCuenta estado;
    private LocalDateTime fechaApertura;
}
