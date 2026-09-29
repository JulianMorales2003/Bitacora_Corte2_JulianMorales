package edu.escuelaing.dosw.brasaviva.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_mesa", nullable = false)
    private Long idMesa;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id")
    @Builder.Default
    private List<ItemPedido> items = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime timestamp;

    public boolean puedeModificarse() {
        return estado == EstadoPedido.RECIBIDO;
    }

    public void agregarItem(ItemPedido item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    public void cambiarEstado(EstadoPedido nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public boolean estaActivo() {
        return estado != EstadoPedido.ENTREGADO && estado != EstadoPedido.CANCELADO;
    }

    public double calcularTotal() {
        if (items == null) {
            return 0.0;
        }
        return items.stream().mapToDouble(ItemPedido::subtotal).sum();
    }

    public int contarCortes() {
        if (items == null) {
            return 0;
        }
        return items.stream()
                .filter(ItemPedido::esCorte)
                .mapToInt(ItemPedido::getCantidad)
                .sum();
    }
}