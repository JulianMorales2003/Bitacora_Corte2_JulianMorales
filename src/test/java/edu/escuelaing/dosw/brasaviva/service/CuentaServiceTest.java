package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.AbrirCuentaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PagoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.CuentaResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.CuentaConPedidosPendientesException;
import edu.escuelaing.dosw.brasaviva.exception.CuentaNoEncontradaException;
import edu.escuelaing.dosw.brasaviva.exception.CuentaYaAbiertaException;
import edu.escuelaing.dosw.brasaviva.exception.EstadoInvalidoException;
import edu.escuelaing.dosw.brasaviva.exception.PagoInsuficienteException;
import edu.escuelaing.dosw.brasaviva.mapper.out.CuentaMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.Cuenta;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.service.impl.CuentaServiceImpl;
import edu.escuelaing.dosw.brasaviva.support.RelojDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    private static final LocalDateTime APERTURA = LocalDateTime.of(2026, 9, 21, 19, 0);

    @Mock
    private IMesaService mesaService;

    @Mock
    private IPedidoService pedidoService;

    @Mock
    private CuentaMapperOut mapperOut;

    private CuentaServiceImpl cuentaService;

    @BeforeEach
    void setUp() {
        cuentaService = new CuentaServiceImpl(mesaService, pedidoService, mapperOut, new RelojDePrueba(APERTURA));
        lenient().when(mapperOut.toDTO(any(Cuenta.class))).thenAnswer(inv -> {
            Cuenta c = inv.getArgument(0);
            return new CuentaResponseDTO(c.getId(), c.getIdMesa(), c.getTotal(), c.getEstado().name(),
                    c.getFechaApertura(), c.getFechaCierre(),
                    c.getMedioPago() == null ? null : c.getMedioPago().name(),
                    c.getMontoRecibido(), c.getCambio());
        });
    }

    private Pedido pedido(EstadoPedido estado, double precio, int cantidad) {
        ItemPedido item = ItemPedido.builder().precioCongelado(precio).cantidad(cantidad).build();
        return Pedido.builder().idMesa(1L).estado(estado).items(List.of(item)).build();
    }

    private void pedidosDeLaMesa(Pedido... pedidos) {
        when(pedidoService.obtenerEntidadesPorMesaDesde(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(pedidos));
    }

    @Test
    @DisplayName("Abrir cuenta - queda ABIERTA en 0 y marca la mesa")
    void abrir_mesaLibre_debeAbrirCuenta() {
        CuentaResponseDTO resultado = cuentaService.abrir(new AbrirCuentaRequestDTO(1L));

        assertEquals(1L, resultado.id());
        assertEquals("ABIERTA", resultado.estado());
        assertEquals(0.0, resultado.total());
        assertEquals(APERTURA, resultado.fechaApertura());
        verify(mesaService, times(1)).abrirCuenta(1L);
    }

    @Test
    @DisplayName("RN-03: abrir cuenta en mesa que ya tiene una - propaga CuentaYaAbiertaException")
    void abrir_mesaConCuenta_debeLanzarExcepcion() {
        doThrow(new CuentaYaAbiertaException("La mesa 1 ya tiene una cuenta abierta"))
                .when(mesaService).abrirCuenta(1L);

        assertThrows(CuentaYaAbiertaException.class, () -> cuentaService.abrir(new AbrirCuentaRequestDTO(1L)));
        verify(mapperOut, never()).toDTO(any(Cuenta.class));
    }

    @Test
    @DisplayName("Consultar cuenta abierta - el total se calcula en vivo sin contar cancelados")
    void obtenerPorId_cuentaAbierta_calculaTotalEnVivo() {
        CuentaResponseDTO abierta = cuentaService.abrir(new AbrirCuentaRequestDTO(1L));
        pedidosDeLaMesa(
                pedido(EstadoPedido.EN_PREPARACION, 58000.0, 2),
                pedido(EstadoPedido.CANCELADO, 12000.0, 5));

        CuentaResponseDTO resultado = cuentaService.obtenerPorId(abierta.id());

        assertEquals(116000.0, resultado.total());
    }

    @Test
    @DisplayName("Consultar cuenta inexistente - CuentaNoEncontradaException")
    void obtenerPorId_inexistente_debeLanzarExcepcion() {
        assertThrows(CuentaNoEncontradaException.class, () -> cuentaService.obtenerPorId(3L));
    }

    @Test
    @DisplayName("Mesa sin cuenta abierta - CuentaNoEncontradaException")
    void obtenerAbiertaPorMesa_sinCuenta_debeLanzarExcepcion() {
        assertThrows(CuentaNoEncontradaException.class, () -> cuentaService.obtenerAbiertaPorMesa(1L));
        verify(mesaService).obtenerEntidad(1L);
    }

    @Test
    @DisplayName("Pagar con todo entregado - cierra la cuenta, calcula el cambio y libera la mesa")
    void registrarPago_todoEntregado_debeCerrarCuenta() {
        CuentaResponseDTO abierta = cuentaService.abrir(new AbrirCuentaRequestDTO(1L));
        pedidosDeLaMesa(
                pedido(EstadoPedido.ENTREGADO, 58000.0, 1),
                pedido(EstadoPedido.ENTREGADO, 12000.0, 2));

        CuentaResponseDTO resultado = cuentaService.registrarPago(abierta.id(), new PagoRequestDTO("EFECTIVO", 100000.0));

        assertEquals("CERRADA", resultado.estado());
        assertEquals(82000.0, resultado.total());
        assertEquals(18000.0, resultado.cambio());
        assertEquals("EFECTIVO", resultado.medioPago());
        assertNotNull(resultado.fechaCierre());
        verify(mesaService, times(1)).cerrarCuenta(1L);
    }

    @Test
    @DisplayName("Pagar con pedidos sin entregar - CuentaConPedidosPendientesException")
    void registrarPago_pedidosPendientes_debeLanzarExcepcion() {
        CuentaResponseDTO abierta = cuentaService.abrir(new AbrirCuentaRequestDTO(1L));
        pedidosDeLaMesa(
                pedido(EstadoPedido.ENTREGADO, 58000.0, 1),
                pedido(EstadoPedido.LISTO, 12000.0, 1));

        CuentaConPedidosPendientesException ex = assertThrows(CuentaConPedidosPendientesException.class,
                () -> cuentaService.registrarPago(abierta.id(), new PagoRequestDTO("TARJETA", 70000.0)));

        assertTrue(ex.getMessage().contains("1 pedido"));
        verify(mesaService, never()).cerrarCuenta(any());
    }

    @Test
    @DisplayName("Pagar menos del total - PagoInsuficienteException")
    void registrarPago_montoInsuficiente_debeLanzarExcepcion() {
        CuentaResponseDTO abierta = cuentaService.abrir(new AbrirCuentaRequestDTO(1L));
        pedidosDeLaMesa(pedido(EstadoPedido.ENTREGADO, 58000.0, 1));

        assertThrows(PagoInsuficienteException.class,
                () -> cuentaService.registrarPago(abierta.id(), new PagoRequestDTO("EFECTIVO", 50000.0)));
    }

    @Test
    @DisplayName("Pagar una cuenta ya cerrada - EstadoInvalidoException")
    void registrarPago_cuentaCerrada_debeLanzarExcepcion() {
        CuentaResponseDTO abierta = cuentaService.abrir(new AbrirCuentaRequestDTO(1L));
        pedidosDeLaMesa(pedido(EstadoPedido.ENTREGADO, 10000.0, 1));
        cuentaService.registrarPago(abierta.id(), new PagoRequestDTO("TARJETA", 10000.0));

        assertThrows(EstadoInvalidoException.class,
                () -> cuentaService.registrarPago(abierta.id(), new PagoRequestDTO("TARJETA", 10000.0)));
    }

    @Test
    @DisplayName("Pagar una cuenta sin pedidos - total 0 y se cierra")
    void registrarPago_sinPedidos_debeCerrarEnCero() {
        CuentaResponseDTO abierta = cuentaService.abrir(new AbrirCuentaRequestDTO(1L));
        pedidosDeLaMesa();

        CuentaResponseDTO resultado = cuentaService.registrarPago(abierta.id(), new PagoRequestDTO("EFECTIVO", 1.0));

        assertEquals(0.0, resultado.total());
        assertEquals("CERRADA", resultado.estado());
    }
}