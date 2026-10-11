package edu.escuelaing.dosw.brasaviva.persistence.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla {@code platos}. Es una clase de infraestructura: solo describe la tabla, no tiene logica de negocio.
 * La logica vive en {@code model.domain.Plato} y el mapper traduce entre ambas.
 */
@Entity
@Table(name = "platos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatoEntity {

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
}
