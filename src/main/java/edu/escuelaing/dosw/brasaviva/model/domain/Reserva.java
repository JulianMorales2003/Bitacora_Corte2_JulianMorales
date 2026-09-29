package edu.escuelaing.dosw.brasaviva.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    public static final long DURACION_MINUTOS = 120;

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

    public boolean estaVigente() {
        return estado == EstadoReserva.ACTIVA
                && fechaHora != null
                && fechaHora.isAfter(LocalDateTime.now());
    }

    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }

    public void reprogramar(LocalDateTime nuevaFecha) {
        this.fechaHora = nuevaFecha;
    }

    public boolean seSolapaCon(LocalDateTime otraFecha) {
        long minutos = Math.abs(Duration.between(fechaHora, otraFecha).toMinutes());
        return minutos < DURACION_MINUTOS;
    }
}