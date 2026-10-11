package edu.escuelaing.dosw.brasaviva.persistence.entity;

import jakarta.persistence.*;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoReserva;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla {@code reservas}. Es una clase de infraestructura: solo describe la tabla, no tiene logica de negocio.
 * La logica vive en {@code model.domain.Reserva} y el mapper traduce entre ambas.
 */
@Entity
@Table(name = "reservas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_mesa", nullable = false)
    private Long idMesa;
    private String cliente;
    private LocalDateTime fechaHora;
    private Integer comensales;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;
}
