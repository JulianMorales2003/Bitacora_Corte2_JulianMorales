package edu.escuelaing.dosw.brasaviva.persistence.entity;

import jakarta.persistence.*;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoCuenta;
import edu.escuelaing.dosw.brasaviva.model.domain.MedioPago;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla {@code cuentas}. Es una clase de infraestructura: solo describe la tabla, no tiene logica de negocio.
 * La logica vive en {@code model.domain.Cuenta} y el mapper traduce entre ambas.
 */
@Entity
@Table(name = "cuentas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_mesa", nullable = false)
    private Long idMesa;
    private Double total;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuenta estado;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    @Enumerated(EnumType.STRING)
    private MedioPago medioPago;
    private Double montoRecibido;
    private Double cambio;
}
