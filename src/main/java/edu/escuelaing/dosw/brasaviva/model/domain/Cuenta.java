package edu.escuelaing.dosw.brasaviva.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cuentas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta {

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

    public double calcularTotal(List<Pedido> pedidos) {
        this.total = pedidos.stream()
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO)
                .mapToDouble(Pedido::calcularTotal)
                .sum();
        return this.total;
    }

    public void registrarPago(MedioPago medio, double monto) {
        this.medioPago = medio;
        this.montoRecibido = monto;
        this.cambio = monto - (total == null ? 0.0 : total);
        cerrarCuenta();
    }

    public void cerrarCuenta() {
        this.estado = EstadoCuenta.CERRADA;
        this.fechaCierre = LocalDateTime.now();
    }

    public boolean estaAbierta() {
        return estado == EstadoCuenta.ABIERTA;
    }
}