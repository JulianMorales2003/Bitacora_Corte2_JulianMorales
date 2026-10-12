package edu.escuelaing.dosw.brasaviva.persistence;

import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import edu.escuelaing.dosw.brasaviva.persistence.adapter.PedidoRepositoryAdapter;
import edu.escuelaing.dosw.brasaviva.persistence.adapter.PlatoRepositoryAdapter;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.PedidoEntityMapperImpl;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.PlatoEntityMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueba los adaptadores contra una base H2 en memoria: dominio entra y sale, la entidad no se ve. */
@DataJpaTest
@Import({PlatoRepositoryAdapter.class, PlatoEntityMapperImpl.class,
        PedidoRepositoryAdapter.class, PedidoEntityMapperImpl.class})
class RepositoryAdapterTest {

    @Autowired private PlatoRepositoryAdapter platoRepository;
    @Autowired private PedidoRepositoryAdapter pedidoRepository;

    @Test
    @DisplayName("save asigna el id generado al objeto de dominio y findById lo recupera")
    void plato_guardar_y_buscar() {
        Plato plato = Plato.builder().nombre("Picanha").precio(58000.0)
                .categoria("CORTE").disponible(true).build();

        Plato guardado = platoRepository.save(plato);

        assertNotNull(guardado.getId());
        assertEquals("Picanha", platoRepository.findById(guardado.getId()).orElseThrow().getNombre());
        assertTrue(platoRepository.existsByNombreIgnoreCase("PICANHA"));
        assertFalse(platoRepository.existsByNombreIgnoreCaseAndIdNot("picanha", guardado.getId()));
        assertEquals(1, platoRepository.findByDisponibleTrue().size());
    }

    @Test
    @DisplayName("delete elimina el plato de la base de datos")
    void plato_eliminar() {
        Plato guardado = platoRepository.save(Plato.builder().nombre("Chorizo").precio(14000.0)
                .categoria("ENTRADA").disponible(true).build());

        platoRepository.delete(guardado);

        assertTrue(platoRepository.findById(guardado.getId()).isEmpty());
    }

    @Test
    @DisplayName("Pedido: save asigna id al pedido y a sus items, y el pedido se recupera completo")
    void pedido_guardar_con_items() {
        Pedido pedido = Pedido.builder().idMesa(1L).estado(EstadoPedido.RECIBIDO)
                .timestamp(LocalDateTime.of(2026, 10, 11, 13, 0))
                .items(new ArrayList<>(List.of(ItemPedido.builder().idPlato(1L).nombrePlato("Picanha")
                        .categoria("CORTE").precioCongelado(58000.0).cantidad(2).build())))
                .build();

        Pedido guardado = pedidoRepository.save(pedido);

        assertNotNull(guardado.getId());
        assertNotNull(guardado.getItems().get(0).getId());
        Pedido leido = pedidoRepository.findById(guardado.getId()).orElseThrow();
        assertEquals(1, leido.getItems().size());
        assertEquals(116000.0, leido.calcularTotal());
        assertEquals(1, pedidoRepository.findByEstado(EstadoPedido.RECIBIDO).size());
    }
}