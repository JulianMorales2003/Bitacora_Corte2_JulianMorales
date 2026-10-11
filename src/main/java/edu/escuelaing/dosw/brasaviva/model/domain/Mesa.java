package edu.escuelaing.dosw.brasaviva.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mesa {

    private Long id;
    private Integer numero;
    private Integer capacidad;
    private EstadoMesa estado;
    private Boolean cuentaAbierta;

    public boolean estaDisponible() {
        return estado == EstadoMesa.DISPONIBLE && !tieneCuentaAbierta();
    }

    public boolean tieneCuentaAbierta() {
        return Boolean.TRUE.equals(cuentaAbierta);
    }

    public void abrirCuenta() {
        this.cuentaAbierta = true;
        this.estado = EstadoMesa.OCUPADA;
    }

    public void cerrarCuenta() {
        this.cuentaAbierta = false;
        this.estado = EstadoMesa.DISPONIBLE;
    }
}