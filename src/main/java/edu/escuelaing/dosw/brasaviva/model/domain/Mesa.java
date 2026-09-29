package edu.escuelaing.dosw.brasaviva.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mesas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Integer numero;
    private Integer capacidad;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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