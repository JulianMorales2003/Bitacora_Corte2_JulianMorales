package edu.escuelaing.dosw.brasaviva.persistence.entity;

import jakarta.persistence.*;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoMesa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla {@code mesas}. Es una clase de infraestructura: solo describe la tabla, no tiene logica de negocio.
 * La logica vive en {@code model.domain.Mesa} y el mapper traduce entre ambas.
 */
@Entity
@Table(name = "mesas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesaEntity {

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
}
