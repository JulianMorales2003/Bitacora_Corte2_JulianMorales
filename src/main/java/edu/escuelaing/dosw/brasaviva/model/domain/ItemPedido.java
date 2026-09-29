package edu.escuelaing.dosw.brasaviva.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items_pedido")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedido {

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

    public double subtotal() {
        if (precioCongelado == null || cantidad == null) {
            return 0.0;
        }
        return precioCongelado * cantidad;
    }

    public boolean esCorte() {
        return Plato.CATEGORIA_CORTE.equals(categoria);
    }

    public boolean esValido() {
        return idPlato != null && cantidad != null && cantidad > 0;
    }
}