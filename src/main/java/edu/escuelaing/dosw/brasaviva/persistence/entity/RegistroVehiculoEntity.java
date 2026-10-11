package edu.escuelaing.dosw.brasaviva.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla {@code registros_vehiculo}. Es una clase de infraestructura: solo describe la tabla, no tiene logica de negocio.
 * La logica vive en {@code model.domain.RegistroVehiculo} y el mapper traduce entre ambas.
 */
@Entity
@Table(name = "registros_vehiculo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroVehiculoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 10)
    private String placa;
    private LocalDateTime entrada;
    private LocalDateTime salida;
    private Double cobro;
}
