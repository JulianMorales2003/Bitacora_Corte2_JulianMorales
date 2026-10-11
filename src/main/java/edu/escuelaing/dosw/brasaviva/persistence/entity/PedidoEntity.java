package edu.escuelaing.dosw.brasaviva.persistence.entity;

import jakarta.persistence.*;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla {@code pedidos}. Es una clase de infraestructura: solo describe la tabla, no tiene logica de negocio.
 * La logica vive en {@code model.domain.Pedido} y el mapper traduce entre ambas.
 */
@Entity
@Table(name = "pedidos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_mesa", nullable = false)
    private Long idMesa;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id")
    @Builder.Default
    private List<ItemPedidoEntity> items = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime timestamp;
}
