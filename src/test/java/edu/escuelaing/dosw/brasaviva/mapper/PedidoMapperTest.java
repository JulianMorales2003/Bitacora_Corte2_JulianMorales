package edu.escuelaing.dosw.brasaviva.mapper;

import edu.escuelaing.dosw.brasaviva.dto.request.ItemPedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ItemPedidoResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PedidoResponseDTO;
import edu.escuelaing.dosw.brasaviva.mapper.in.PedidoMapperInImpl;
import edu.escuelaing.dosw.brasaviva.mapper.out.PedidoMapperOutImpl;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.model.domain.TerminoCoccion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoMapperTest {

    private final PedidoMapperInImpl mapperIn = new PedidoMapperInImpl();
    private final PedidoMapperOutImpl mapperOut = new PedidoMapperOutImpl();

    @Test
    @DisplayName("toDomain (Pedido): copia idMesa y items, sin id, estado ni timestamp")
    void toDomainPedido_debeCopiarCampos() {
        PedidoRequestDTO dto = new PedidoRequestDTO(1L,
                List.of(new ItemPedidoRequestDTO(1L, 2, "TRES_CUARTOS", "sin sal")));

        Pedido resultado = mapperIn.toDomain(dto);

        assertNull(resultado.getId());
        assertNull(resultado.getEstado());
        assertNull(resultado.getTimestamp());
        assertEquals(1L, resultado.getIdMesa());
        assertEquals(1, resultado.getItems().size());
        assertEquals(2, resultado.getItems().get(0).getCantidad());
    }

    @Test
    @DisplayName("toDomain (ItemPedido): convierte el termino a enum, sin nombrePlato ni precio")
    void toDomainItem_debeConvertirEnum() {
        ItemPedidoRequestDTO dto = new ItemPedidoRequestDTO(1L, 2, "TRES_CUARTOS", "sin sal");

        ItemPedido resultado = mapperIn.toDomain(dto);

        assertNull(resultado.getId());
        assertNull(resultado.getNombrePlato());
        assertNull(resultado.getCategoria());
        assertNull(resultado.getPrecioCongelado());
        assertEquals(1L, resultado.getIdPlato());
        assertEquals(2, resultado.getCantidad());
        assertEquals(TerminoCoccion.TRES_CUARTOS, resultado.getTerminoCoccion());
        assertEquals("sin sal", resultado.getObservaciones());
    }

    @Test
    @DisplayName("toDomain (ItemPedido): sin termino de coccion queda null")
    void toDomainItem_sinTermino_debeQuedarNull() {
        ItemPedidoRequestDTO dto = new ItemPedidoRequestDTO(3L, 1, null, null);

        ItemPedido resultado = mapperIn.toDomain(dto);

        assertNull(resultado.getTerminoCoccion());
    }

    @Test
    @DisplayName("toDTO (Pedido): calcula el total con calcularTotal()")
    void toDTOPedido_debeCalcularTotal() {
        ItemPedido item = ItemPedido.builder()
                .id(1L).idPlato(1L).nombrePlato("Picanha")
                .precioCongelado(58000.0).cantidad(2)
                .terminoCoccion(TerminoCoccion.AZUL)
                .build();
        Pedido pedido = Pedido.builder()
                .id(10L).idMesa(1L).items(List.of(item))
                .estado(EstadoPedido.RECIBIDO)
                .timestamp(LocalDateTime.of(2026, 9, 22, 20, 0))
                .build();

        PedidoResponseDTO resultado = mapperOut.toDTO(pedido);

        assertEquals(10L, resultado.id());
        assertEquals("RECIBIDO", resultado.estado());
        assertEquals(1, resultado.items().size());
        assertEquals(116000.0, resultado.total());
    }

    @Test
    @DisplayName("toDTO (ItemPedido): calcula el subtotal con subtotal()")
    void toDTOItem_debeCalcularSubtotal() {
        ItemPedido item = ItemPedido.builder()
                .id(1L).idPlato(1L).nombrePlato("Picanha")
                .precioCongelado(58000.0).cantidad(3)
                .terminoCoccion(TerminoCoccion.BIEN_ASADO)
                .build();

        ItemPedidoResponseDTO resultado = mapperOut.toDTO(item);

        assertEquals("Picanha", resultado.nombrePlato());
        assertEquals("BIEN_ASADO", resultado.terminoCoccion());
        assertEquals(174000.0, resultado.subtotal());
    }
}