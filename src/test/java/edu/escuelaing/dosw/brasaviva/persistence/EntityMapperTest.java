package edu.escuelaing.dosw.brasaviva.persistence;

import edu.escuelaing.dosw.brasaviva.model.domain.EstadoMesa;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import edu.escuelaing.dosw.brasaviva.model.domain.TerminoCoccion;
import edu.escuelaing.dosw.brasaviva.persistence.entity.MesaEntity;
import edu.escuelaing.dosw.brasaviva.persistence.entity.PedidoEntity;
import edu.escuelaing.dosw.brasaviva.persistence.entity.PlatoEntity;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.MesaEntityMapperImpl;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.PedidoEntityMapperImpl;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.PlatoEntityMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EntityMapperTest {

    private final PlatoEntityMapperImpl platoMapper = new PlatoEntityMapperImpl();
    private final MesaEntityMapperImpl mesaMapper = new MesaEntityMapperImpl();
    private final PedidoEntityMapperImpl pedidoMapper = new PedidoEntityMapperImpl();

    @Test
    @DisplayName("Plato: dominio -> entidad -> dominio conserva todos los campos")
    void plato_ida_y_vuelta() {
        Plato plato = new Plato(1L, "Picanha", 58000.0, "CORTE", "300 g a la brasa", true, 20);

        PlatoEntity entidad = platoMapper.toEntity(plato);
        Plato vuelta = platoMapper.toDomain(entidad);

        assertEquals("Picanha", entidad.getNombre());
        assertEquals(plato, vuelta);
    }

    @Test
    @DisplayName("Mesa: el estado y la cuenta abierta viajan a la entidad y regresan")
    void mesa_ida_y_vuelta() {
        Mesa mesa = new Mesa(2L, 5, 4, EstadoMesa.OCUPADA, true);

        MesaEntity entidad = mesaMapper.toEntity(mesa);

        assertEquals(EstadoMesa.OCUPADA, entidad.getEstado());
        assertEquals(mesa, mesaMapper.toDomain(entidad));
    }

    @Test
    @DisplayName("Pedido: los items se mapean a ItemPedidoEntity y regresan")
    void pedido_con_items_ida_y_vuelta() {
        ItemPedido item = ItemPedido.builder()
                .id(7L).idPlato(1L).nombrePlato("Picanha").categoria("CORTE")
                .precioCongelado(58000.0).cantidad(2)
                .terminoCoccion(TerminoCoccion.TRES_CUARTOS).build();
        Pedido pedido = Pedido.builder()
                .id(3L).idMesa(2L).estado(EstadoPedido.RECIBIDO)
                .timestamp(LocalDateTime.of(2026, 10, 11, 13, 0))
                .items(List.of(item)).build();

        PedidoEntity entidad = pedidoMapper.toEntity(pedido);
        Pedido vuelta = pedidoMapper.toDomain(entidad);

        assertEquals(1, entidad.getItems().size());
        assertEquals(7L, entidad.getItems().get(0).getId());
        assertEquals(pedido, vuelta);
        assertEquals(116000.0, vuelta.calcularTotal());
    }

    @Test
    @DisplayName("Un dominio nuevo (sin id) produce una entidad sin id para que la BD lo genere")
    void dominio_nuevo_no_tiene_id() {
        Plato nuevo = Plato.builder().nombre("Limonada").precio(9000.0).categoria("BEBIDA").build();

        assertNull(platoMapper.toEntity(nuevo).getId());
    }
}