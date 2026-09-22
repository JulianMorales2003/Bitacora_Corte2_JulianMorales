package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.config.BrasaVivaProperties;
import edu.escuelaing.dosw.brasaviva.dto.request.CambioEstadoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.ItemPedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ItemPedidoResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PedidoResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.CapacidadParrillaExcedidaException;
import edu.escuelaing.dosw.brasaviva.exception.CorteFueraDeHorarioException;
import edu.escuelaing.dosw.brasaviva.exception.EstadoInvalidoException;
import edu.escuelaing.dosw.brasaviva.exception.MesaNoEncontradaException;
import edu.escuelaing.dosw.brasaviva.exception.MesaSinCuentaAbiertaException;
import edu.escuelaing.dosw.brasaviva.exception.PedidoNoEncontradoException;
import edu.escuelaing.dosw.brasaviva.exception.PedidoNoModificableException;
import edu.escuelaing.dosw.brasaviva.exception.PlatoNoDisponibleException;
import edu.escuelaing.dosw.brasaviva.exception.TerminoCoccionRequeridoException;
import edu.escuelaing.dosw.brasaviva.mapper.in.PedidoMapperIn;
import edu.escuelaing.dosw.brasaviva.mapper.out.PedidoMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoMesa;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import edu.escuelaing.dosw.brasaviva.model.domain.TerminoCoccion;
import edu.escuelaing.dosw.brasaviva.service.impl.PedidoServiceImpl;
import edu.escuelaing.dosw.brasaviva.support.RelojDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private IPlatoService platoService;

    @Mock
    private IMesaService mesaService;

    @Mock
    private PedidoMapperIn mapperIn;

    @Mock
    private PedidoMapperOut mapperOut;

    private RelojDePrueba reloj;
    private PedidoServiceImpl pedidoService;

    private Mesa mesaConCuenta;
    private Plato picanha;
    private Plato tomahawk;
    private Plato limonada;

    @BeforeEach
    void setUp() {
        reloj = new RelojDePrueba(LocalDateTime.of(2026, 9, 21, 13, 0));
        BrasaVivaProperties propiedades = new BrasaVivaProperties(8, LocalTime.of(22, 0), 30, 25, 20, 3000.0);
        pedidoService = new PedidoServiceImpl(platoService, mesaService, mapperIn, mapperOut, propiedades, reloj);

        mesaConCuenta = new Mesa(1L, 1, 4, EstadoMesa.OCUPADA, true);
        picanha = new Plato(1L, "Picanha", 58000.0, "CORTE", null, true, 20);
        tomahawk = new Plato(2L, "Tomahawk", 145000.0, "CORTE", null, true, 35);
        limonada = new Plato(3L, "Limonada de coco", 12000.0, "BEBIDA", null, true, 5);

        lenient().when(mesaService.obtenerEntidad(1L)).thenReturn(mesaConCuenta);
        lenient().when(platoService.obtenerEntidad(1L)).thenReturn(picanha);
        lenient().when(platoService.obtenerEntidad(2L)).thenReturn(tomahawk);
        lenient().when(platoService.obtenerEntidad(3L)).thenReturn(limonada);

        lenient().when(mapperIn.toDomain(any(ItemPedidoRequestDTO.class))).thenAnswer(inv -> aItem(inv.getArgument(0)));
        lenient().when(mapperIn.toDomain(any(PedidoRequestDTO.class))).thenAnswer(inv -> {
            PedidoRequestDTO d = inv.getArgument(0);
            List<ItemPedido> items = new ArrayList<>(d.items().stream().map(this::aItem).toList());
            return Pedido.builder().idMesa(d.idMesa()).items(items).build();
        });
        lenient().when(mapperOut.toDTO(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            List<ItemPedidoResponseDTO> items = p.getItems().stream()
                    .map(i -> new ItemPedidoResponseDTO(i.getId(), i.getIdPlato(), i.getNombrePlato(),
                            i.getPrecioCongelado(), i.getCantidad(),
                            i.getTerminoCoccion() == null ? null : i.getTerminoCoccion().name(),
                            i.getObservaciones(), i.subtotal()))
                    .toList();
            return new PedidoResponseDTO(p.getId(), p.getIdMesa(), items, p.getEstado().name(),
                    p.getTimestamp(), p.calcularTotal());
        });
    }

    private ItemPedido aItem(ItemPedidoRequestDTO d) {
        return ItemPedido.builder()
                .idPlato(d.idPlato())
                .cantidad(d.cantidad())
                .terminoCoccion(d.terminoCoccion() == null ? null : TerminoCoccion.valueOf(d.terminoCoccion()))
                .observaciones(d.observaciones())
                .build();
    }

    private PedidoRequestDTO pedido(ItemPedidoRequestDTO... items) {
        return new PedidoRequestDTO(1L, List.of(items));
    }

    private ItemPedidoRequestDTO item(Long idPlato, int cantidad, String termino) {
        return new ItemPedidoRequestDTO(idPlato, cantidad, termino, null);
    }

    private CambioEstadoRequestDTO estado(String estado) {
        return new CambioEstadoRequestDTO(estado);
    }

    @Test
    @DisplayName("Crear pedido valido - queda RECIBIDO con total calculado")
    void crear_pedidoValido_debeQuedarRecibido() {
        PedidoResponseDTO resultado = pedidoService.crear(pedido(
                item(1L, 2, "TRES_CUARTOS"),
                item(3L, 2, null)));

        assertEquals(1L, resultado.id());
        assertEquals("RECIBIDO", resultado.estado());
        assertEquals(2 * 58000.0 + 2 * 12000.0, resultado.total());
        assertEquals(LocalDateTime.of(2026, 9, 21, 13, 0), resultado.timestamp());
        verify(mapperIn, times(1)).toDomain(any(PedidoRequestDTO.class));
        verify(platoService, times(2)).obtenerEntidad(any());
    }

    @Test
    @DisplayName("RN-04: el precio queda congelado aunque el plato cambie despues")
    void crear_precioCongelado_noCambiaConElPlato() {
        PedidoResponseDTO creado = pedidoService.crear(pedido(item(1L, 1, "AZUL")));

        picanha.setPrecio(99000.0);

        PedidoResponseDTO consultado = pedidoService.obtenerPorId(creado.id());
        assertEquals(58000.0, consultado.items().get(0).precioCongelado());
        assertEquals(58000.0, consultado.total());
    }

    @Test
    @DisplayName("Crear pedido en mesa sin cuenta abierta - MesaSinCuentaAbiertaException")
    void crear_mesaSinCuenta_debeLanzarExcepcion() {
        mesaConCuenta.cerrarCuenta();

        assertThrows(MesaSinCuentaAbiertaException.class,
                () -> pedidoService.crear(pedido(item(3L, 1, null))));
        verify(mapperIn, never()).toDomain(any(PedidoRequestDTO.class));
    }

    @Test
    @DisplayName("Crear pedido en mesa inexistente - MesaNoEncontradaException")
    void crear_mesaInexistente_debeLanzarExcepcion() {
        when(mesaService.obtenerEntidad(9L)).thenThrow(new MesaNoEncontradaException("Mesa no encontrada: 9"));

        assertThrows(MesaNoEncontradaException.class,
                () -> pedidoService.crear(new PedidoRequestDTO(9L, List.of(item(3L, 1, null)))));
    }

    @Test
    @DisplayName("RN-02: pedir un plato agotado - PlatoNoDisponibleException")
    void crear_platoAgotado_debeLanzarExcepcion() {
        limonada.cambiarDisponibilidad(false);

        PlatoNoDisponibleException ex = assertThrows(PlatoNoDisponibleException.class,
                () -> pedidoService.crear(pedido(item(3L, 1, null))));
        assertTrue(ex.getMessage().contains("Limonada"));
        assertTrue(pedidoService.obtenerTodos(null).isEmpty());
    }

    @Test
    @DisplayName("RN-P01: pedir un corte sin termino de coccion - TerminoCoccionRequeridoException")
    void crear_corteSinTermino_debeLanzarExcepcion() {
        assertThrows(TerminoCoccionRequeridoException.class,
                () -> pedidoService.crear(pedido(item(1L, 1, null))));
    }

    @Test
    @DisplayName("El termino de coccion se ignora en platos que no son corte")
    void crear_terminoEnNoCorte_seIgnora() {
        PedidoResponseDTO resultado = pedidoService.crear(pedido(item(3L, 1, "AZUL")));

        assertNull(resultado.items().get(0).terminoCoccion());
    }

    @Test
    @DisplayName("RN-P03: corte lento en los ultimos 30 min antes del cierre - CorteFueraDeHorarioException")
    void crear_corteLentoCercaDelCierre_debeLanzarExcepcion() {
        reloj.fijar(LocalDateTime.of(2026, 9, 21, 21, 40));

        assertThrows(CorteFueraDeHorarioException.class,
                () -> pedidoService.crear(pedido(item(2L, 1, "TRES_CUARTOS"))));
    }

    @Test
    @DisplayName("RN-P03: un corte rapido si se puede pedir cerca del cierre")
    void crear_corteRapidoCercaDelCierre_esValido() {
        reloj.fijar(LocalDateTime.of(2026, 9, 21, 21, 40));

        assertDoesNotThrow(() -> pedidoService.crear(pedido(item(1L, 1, "AZUL"))));
    }

    @Test
    @DisplayName("RN-P03: un corte lento si se puede pedir antes de la ventana de cierre")
    void crear_corteLentoTemprano_esValido() {
        reloj.fijar(LocalDateTime.of(2026, 9, 21, 21, 29));

        assertDoesNotThrow(() -> pedidoService.crear(pedido(item(2L, 1, "BIEN_ASADO"))));
    }

    @Test
    @DisplayName("RN-01: agregar item a un pedido RECIBIDO - se suma al total")
    void agregarItem_pedidoRecibido_debeAgregar() {
        PedidoResponseDTO creado = pedidoService.crear(pedido(item(3L, 1, null)));

        PedidoResponseDTO resultado = pedidoService.agregarItem(creado.id(), item(1L, 1, "AZUL"));

        assertEquals(2, resultado.items().size());
        assertEquals(12000.0 + 58000.0, resultado.total());
    }

    @Test
    @DisplayName("RN-01: agregar item a un pedido en cocina - PedidoNoModificableException")
    void agregarItem_pedidoEnPreparacion_debeLanzarExcepcion() {
        PedidoResponseDTO creado = pedidoService.crear(pedido(item(3L, 1, null)));
        pedidoService.cambiarEstado(creado.id(), estado("EN_PREPARACION"));

        assertThrows(PedidoNoModificableException.class,
                () -> pedidoService.agregarItem(creado.id(), item(3L, 1, null)));
    }

    @Test
    @DisplayName("Flujo completo RECIBIDO -> EN_PREPARACION -> LISTO -> ENTREGADO")
    void cambiarEstado_flujoCompleto_debeAvanzar() {
        PedidoResponseDTO creado = pedidoService.crear(pedido(item(1L, 1, "AZUL")));

        pedidoService.cambiarEstado(creado.id(), estado("EN_PREPARACION"));
        pedidoService.cambiarEstado(creado.id(), estado("LISTO"));
        PedidoResponseDTO entregado = pedidoService.cambiarEstado(creado.id(), estado("ENTREGADO"));

        assertEquals("ENTREGADO", entregado.estado());
    }

    @Test
    @DisplayName("Saltarse un estado (RECIBIDO -> LISTO) - EstadoInvalidoException")
    void cambiarEstado_saltandoEstado_debeLanzarExcepcion() {
        PedidoResponseDTO creado = pedidoService.crear(pedido(item(3L, 1, null)));

        EstadoInvalidoException ex = assertThrows(EstadoInvalidoException.class,
                () -> pedidoService.cambiarEstado(creado.id(), estado("LISTO")));
        assertTrue(ex.getMessage().contains("RECIBIDO -> LISTO"));
    }

    @Test
    @DisplayName("Un pedido ENTREGADO ya no cambia de estado")
    void cambiarEstado_desdeEntregado_debeLanzarExcepcion() {
        PedidoResponseDTO creado = pedidoService.crear(pedido(item(3L, 1, null)));
        pedidoService.cambiarEstado(creado.id(), estado("EN_PREPARACION"));
        pedidoService.cambiarEstado(creado.id(), estado("LISTO"));
        pedidoService.cambiarEstado(creado.id(), estado("ENTREGADO"));

        assertThrows(EstadoInvalidoException.class,
                () -> pedidoService.cambiarEstado(creado.id(), estado("CANCELADO")));
    }

    @Test
    @DisplayName("RN-P02: la parrilla no acepta mas de 8 cortes simultaneos")
    void cambiarEstado_parrillaLlena_debeLanzarExcepcion() {
        PedidoResponseDTO primero = pedidoService.crear(pedido(item(1L, 5, "AZUL")));
        PedidoResponseDTO segundo = pedidoService.crear(pedido(item(1L, 4, "BIEN_ASADO"), item(3L, 3, null)));
        pedidoService.cambiarEstado(primero.id(), estado("EN_PREPARACION"));

        CapacidadParrillaExcedidaException ex = assertThrows(CapacidadParrillaExcedidaException.class,
                () -> pedidoService.cambiarEstado(segundo.id(), estado("EN_PREPARACION")));

        assertTrue(ex.getMessage().contains("5/8"));
        assertEquals("RECIBIDO", pedidoService.obtenerPorId(segundo.id()).estado());
    }

    @Test
    @DisplayName("RN-P02: cuando un pedido sale de la parrilla, se libera cupo")
    void cambiarEstado_liberaCupoAlQuedarListo() {
        PedidoResponseDTO primero = pedidoService.crear(pedido(item(1L, 5, "AZUL")));
        PedidoResponseDTO segundo = pedidoService.crear(pedido(item(1L, 4, "AZUL")));
        pedidoService.cambiarEstado(primero.id(), estado("EN_PREPARACION"));
        pedidoService.cambiarEstado(primero.id(), estado("LISTO"));

        PedidoResponseDTO resultado = pedidoService.cambiarEstado(segundo.id(), estado("EN_PREPARACION"));

        assertEquals("EN_PREPARACION", resultado.estado());
    }

    @Test
    @DisplayName("Los platos que no son corte no ocupan parrilla")
    void cambiarEstado_sinCortes_noOcupaParrilla() {
        PedidoResponseDTO lleno = pedidoService.crear(pedido(item(1L, 8, "AZUL")));
        PedidoResponseDTO bebidas = pedidoService.crear(pedido(item(3L, 10, null)));
        pedidoService.cambiarEstado(lleno.id(), estado("EN_PREPARACION"));

        assertDoesNotThrow(() -> pedidoService.cambiarEstado(bebidas.id(), estado("EN_PREPARACION")));
    }

    @Test
    @DisplayName("Cambiar estado de un pedido inexistente - PedidoNoEncontradoException")
    void cambiarEstado_pedidoInexistente_debeLanzarExcepcion() {
        assertThrows(PedidoNoEncontradoException.class,
                () -> pedidoService.cambiarEstado(50L, estado("LISTO")));
    }

    @Test
    @DisplayName("Cancelar pedido RECIBIDO - queda CANCELADO (no se borra)")
    void cancelar_pedidoRecibido_debeQuedarCancelado() {
        PedidoResponseDTO creado = pedidoService.crear(pedido(item(3L, 1, null)));

        pedidoService.cancelar(creado.id());

        assertEquals("CANCELADO", pedidoService.obtenerPorId(creado.id()).estado());
    }

    @Test
    @DisplayName("Cancelar pedido que ya esta en cocina - PedidoNoModificableException")
    void cancelar_pedidoEnCocina_debeLanzarExcepcion() {
        PedidoResponseDTO creado = pedidoService.crear(pedido(item(3L, 1, null)));
        pedidoService.cambiarEstado(creado.id(), estado("EN_PREPARACION"));

        assertThrows(PedidoNoModificableException.class, () -> pedidoService.cancelar(creado.id()));
    }

    @Test
    @DisplayName("Sin pedidos - lista vacia")
    void obtenerTodos_sinPedidos_debeRetornarListaVacia() {
        assertTrue(pedidoService.obtenerTodos(null).isEmpty());
        assertTrue(pedidoService.obtenerTableroCocina().isEmpty());
    }

    @Test
    @DisplayName("Tablero de cocina - solo RECIBIDO y EN_PREPARACION, en orden de llegada")
    void obtenerTableroCocina_debeFiltrarYOrdenar() {
        PedidoResponseDTO p1 = pedidoService.crear(pedido(item(3L, 1, null)));
        reloj.adelantar(java.time.Duration.ofMinutes(5));
        PedidoResponseDTO p2 = pedidoService.crear(pedido(item(3L, 1, null)));
        reloj.adelantar(java.time.Duration.ofMinutes(5));
        PedidoResponseDTO p3 = pedidoService.crear(pedido(item(3L, 1, null)));
        pedidoService.cambiarEstado(p1.id(), estado("EN_PREPARACION"));
        pedidoService.cancelar(p2.id());

        List<PedidoResponseDTO> tablero = pedidoService.obtenerTableroCocina();

        assertEquals(List.of(p1.id(), p3.id()), tablero.stream().map(PedidoResponseDTO::id).toList());
    }

    @Test
    @DisplayName("Pedidos activos por mesa - excluye entregados y cancelados")
    void obtenerActivosPorMesa_debeExcluirTerminados() {
        PedidoResponseDTO activo = pedidoService.crear(pedido(item(3L, 1, null)));
        PedidoResponseDTO cancelado = pedidoService.crear(pedido(item(3L, 1, null)));
        pedidoService.cancelar(cancelado.id());

        List<PedidoResponseDTO> activos = pedidoService.obtenerActivosPorMesa(1L);

        assertEquals(1, activos.size());
        assertEquals(activo.id(), activos.get(0).id());
    }

    @Test
    @DisplayName("Filtrar pedidos por estado")
    void obtenerTodos_filtroEstado_debeFiltrar() {
        pedidoService.crear(pedido(item(3L, 1, null)));
        PedidoResponseDTO cancelado = pedidoService.crear(pedido(item(3L, 1, null)));
        pedidoService.cancelar(cancelado.id());

        assertEquals(1, pedidoService.obtenerTodos("cancelado").size());
        assertEquals(2, pedidoService.obtenerTodos(null).size());
    }

    @Test
    @DisplayName("Pedidos de una mesa desde una fecha - usado por la cuenta")
    void obtenerEntidadesPorMesaDesde_debeFiltrarPorFecha() {
        pedidoService.crear(pedido(item(3L, 1, null)));
        LocalDateTime apertura = LocalDateTime.of(2026, 9, 21, 14, 0);
        reloj.fijar(apertura);
        pedidoService.crear(pedido(item(3L, 1, null)));

        List<Pedido> desdeApertura = pedidoService.obtenerEntidadesPorMesaDesde(1L, apertura);

        assertEquals(1, desdeApertura.size());
        assertEquals(EstadoPedido.RECIBIDO, desdeApertura.get(0).getEstado());
    }
}