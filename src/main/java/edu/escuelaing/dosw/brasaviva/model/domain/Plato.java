package edu.escuelaing.dosw.brasaviva.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plato {

    public static final String CATEGORIA_CORTE = "CORTE";

    private Long id;
    private String nombre;
    private Double precio;
    private String categoria;
    private String descripcion;
    private Boolean disponible;
    private Integer tiempoPreparacionMin;

    public boolean esValido() {
        return nombre != null && !nombre.isBlank() && precio != null && precio > 0;
    }

    public void cambiarDisponibilidad(boolean disponible) {
        this.disponible = disponible;
    }

    public boolean estaDisponible() {
        return Boolean.TRUE.equals(disponible);
    }

    public boolean esCorte() {
        return CATEGORIA_CORTE.equals(categoria);
    }
}