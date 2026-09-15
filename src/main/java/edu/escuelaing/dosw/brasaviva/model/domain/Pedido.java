package edu.escuelaing.dosw.brasaviva.model.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Pedido {
    private Long id;
    private Long idMesa;
    private List<ItemPedido> items;
    private EstadoPedido estado;
    private LocalDateTime timestamp;
}
