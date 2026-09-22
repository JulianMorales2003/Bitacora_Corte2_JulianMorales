package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.response.IngresosResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoPopularDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ResumenDiaDTO;
import edu.escuelaing.dosw.brasaviva.exception.RangoFechasInvalidoException;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.service.impl.ReporteServiceImpl;
import edu.escuelaing.dosw.brasaviva.support.RelojDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    private static final LocalDateTime HOY = LocalDateTime.of(2026, 9, 21, 20, 0);
    private static final LocalDateTime AYER = HOY.minusDays(1);

    @Mock
    private IPedidoService pedidoService;

    @Mock
    private IMesaService mesaService;

    private ReporteServiceImpl reporteService;

    @BeforeEach
    void setUp() {
        reporteService = new ReporteServiceImpl(pedidoService, mesaService, new RelojDePrueba(HOY));
    }

    private ItemPedido item(String nombre, String categoria, double precio, int cantidad) {
        return ItemPedido.builder().nombrePlato(nombre).categoria(categoria)
                .precioCongelado(precio).cantidad(cantidad).build();
    }

    private Pedido pedido(LocalDateTime fecha, EstadoPedido estado, ItemPedido... items) {
        return Pedido.builder().idMesa(1L).timestamp(fecha).estado(estado).items(List.of(items)).build();
    }

    private void conPedidos() {
        when(pedidoService.obtenerEntidades()).thenReturn(List.of(
                pedido(HOY, EstadoPedido.ENTREGADO,
                        item("Picanha", "CORTE", 58000.0, 2),
                        item("Limonada", "BEBIDA", 12000.0, 2)),
                pedido(HOY, EstadoPedido.EN_PREPARACION,
                        item("Picanha", "CORTE", 58000.0, 1)),
                pedido(HOY, EstadoPedido.CANCELADO,
                        item("Tomahawk", "CORTE", 145000.0, 3)),
                pedido(AYER, EstadoPedido.ENTREGADO,
                        item("Churrasco", "CORTE", 52000.0, 1))));
    }

    @Test
    @DisplayName("Resumen del dia - solo pedidos de hoy y sin contar cancelados como venta")
    void resumenDelDia_debeCalcularConStreams() {
        conPedidos();
        when(mesaService.contarConCuentaAbierta()).thenReturn(2L);

        ResumenDiaDTO resumen = reporteService.resumenDelDia();

        assertEquals(2, resumen.totalPedidos());
        assertEquals(3 * 58000.0 + 2 * 12000.0, resumen.ingresoTotal());
        assertEquals(3L, resumen.platosMasPedidos().get("Picanha"));
        assertNull(resumen.platosMasPedidos().get("Tomahawk"));
        assertEquals(1L, resumen.pedidosPorEstado().get("CANCELADO"));
        assertEquals(2L, resumen.mesasConCuentaAbierta());
    }

    @Test
    @DisplayName("Resumen sin pedidos - todo en cero y mapas vacios")
    void resumenDelDia_sinPedidos_debeRetornarCeros() {
        when(pedidoService.obtenerEntidades()).thenReturn(List.of());

        ResumenDiaDTO resumen = reporteService.resumenDelDia();

        assertEquals(0, resumen.totalPedidos());
        assertEquals(0.0, resumen.ingresoTotal());
        assertTrue(resumen.platosMasPedidos().isEmpty());
    }

    @Test
    @DisplayName("Platos populares - ordenados por cantidad vendida y limitados al top")
    void platosPopulares_debeOrdenarYLimitar() {
        conPedidos();

        List<PlatoPopularDTO> top = reporteService.platosPopulares(2);

        assertEquals(2, top.size());
        assertEquals(new PlatoPopularDTO("Picanha", 3), top.get(0));
        assertEquals(new PlatoPopularDTO("Limonada", 2), top.get(1));
    }

    @Test
    @DisplayName("Ingresos por categoria en un rango - excluye cancelados y fechas fuera del rango")
    void ingresos_porCategoria_debeAgrupar() {
        conPedidos();

        IngresosResponseDTO resultado = reporteService.ingresos(HOY.toLocalDate(), HOY.toLocalDate());

        assertEquals(3 * 58000.0, resultado.ingresosPorCategoria().get("CORTE"));
        assertEquals(2 * 12000.0, resultado.ingresosPorCategoria().get("BEBIDA"));
        assertEquals(3 * 58000.0 + 2 * 12000.0, resultado.total());
    }

    @Test
    @DisplayName("Ingresos con fecha inicial posterior a la final - RangoFechasInvalidoException")
    void ingresos_rangoInvalido_debeLanzarExcepcion() {
        LocalDate hoy = HOY.toLocalDate();

        assertThrows(RangoFechasInvalidoException.class, () -> reporteService.ingresos(hoy, hoy.minusDays(1)));
        verify(pedidoService, never()).obtenerEntidades();
    }
}