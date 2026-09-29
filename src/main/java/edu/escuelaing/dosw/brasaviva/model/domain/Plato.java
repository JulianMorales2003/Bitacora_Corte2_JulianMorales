package edu.escuelaing.dosw.brasaviva.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plato {

    public static final String CATEGORIA_CORTE = "CORTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private Double precio;
    @Column(nullable = false)
    private String categoria;
    @Column(length = 500)
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