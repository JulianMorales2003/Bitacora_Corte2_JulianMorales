package edu.escuelaing.dosw.brasaviva.persistence.entity;

import jakarta.persistence.*;
import edu.escuelaing.dosw.brasaviva.model.domain.TerminoCoccion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla {@code items_pedido}. Es una clase de infraestructura: solo describe la tabla, no tiene logica de negocio.
 * La logica vive en {@code model.domain.ItemPedido} y el mapper traduce entre ambas.
 */
@Entity
@Table(name = "items_pedido")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_plato", nullable = false)
    private Long idPlato;
    private String nombrePlato;
    private String categoria;
    private Double precioCongelado;
    private Integer cantidad;
    @Enumerated(EnumType.STRING)
    private TerminoCoccion terminoCoccion;
    @Column(length = 300)
    private String observaciones;
}
