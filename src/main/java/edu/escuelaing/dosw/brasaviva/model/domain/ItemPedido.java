package edu.escuelaing.dosw.brasaviva.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedido {

    private Long id;
    private Long idPlato;
    private String nombrePlato;
    private String categoria;
    private Double precioCongelado;
    private Integer cantidad;
    private TerminoCoccion terminoCoccion;
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