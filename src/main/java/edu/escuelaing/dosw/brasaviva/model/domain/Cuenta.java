package edu.escuelaing.dosw.brasaviva.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta {

    private Long id;
    private Long idMesa;
    private Double total;
    private EstadoCuenta estado;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
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