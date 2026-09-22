package edu.escuelaing.dosw.brasaviva.service.impl;

import edu.escuelaing.dosw.brasaviva.config.BrasaVivaProperties;
import edu.escuelaing.dosw.brasaviva.dto.request.CambioEstadoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.ItemPedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PedidoResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.CapacidadParrillaExcedidaException;
import edu.escuelaing.dosw.brasaviva.exception.CorteFueraDeHorarioException;
import edu.escuelaing.dosw.brasaviva.exception.EstadoInvalidoException;
import edu.escuelaing.dosw.brasaviva.exception.MesaSinCuentaAbiertaException;
import edu.escuelaing.dosw.brasaviva.exception.PedidoNoEncontradoException;
import edu.escuelaing.dosw.brasaviva.exception.PedidoNoModificableException;
import edu.escuelaing.dosw.brasaviva.exception.PlatoNoDisponibleException;
import edu.escuelaing.dosw.brasaviva.exception.TerminoCoccionRequeridoException;
import edu.escuelaing.dosw.brasaviva.mapper.in.PedidoMapperIn;
import edu.escuelaing.dosw.brasaviva.mapper.out.PedidoMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import edu.escuelaing.dosw.brasaviva.service.IMesaService;
import edu.escuelaing.dosw.brasaviva.service.IPedidoService;
import edu.escuelaing.dosw.brasaviva.service.IPlatoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class PedidoServiceImpl implements IPedidoService {

    private final IPlatoService platoService;
    private final IMesaService mesaService;
    private final PedidoMapperIn mapperIn;
    private final PedidoMapperOut mapperOut;
    private final BrasaVivaProperties propiedades;
    private final Clock clock;

    private final Map<Long, Pedido> pedidos = new ConcurrentHashMap<>();
    private final AtomicLong secuenciaPedidos = new AtomicLong(0);
    private final AtomicLong secuenciaItems = new AtomicLong(0);

    @Override
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        Mesa mesa = mesaService.obtenerEntidad(dto.idMesa());
        if (!mesa.tieneCuentaAbierta()) {
            throw new MesaSinCuentaAbiertaException(
                    "La mesa " + mesa.getNumero() + " no tiene cuenta abierta. Abra la cuenta antes de pedir");
        }

        Pedido pedido = mapperIn.toDomain(dto);
        pedido.getItems().forEach(this::completarItem);
        pedido.setId(secuenciaPedidos.incrementAndGet());
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.setTimestamp(LocalDateTime.now(clock));
        pedidos.put(pedido.getId(), pedido);

        log.info("Pedido {} confirmado en mesa {}: {} items, total={}",
                pedido.getId(), mesa.getNumero(), pedido.getItems().size(), pedido.calcularTotal());
        return mapperOut.toDTO(pedido);
    }

    @Override
    public PedidoResponseDTO agregarItem(Long idPedido, ItemPedidoRequestDTO dto) {
        Pedido pedido = buscarOLanzar(idPedido);
        if (!pedido.puedeModificarse()) {
            throw new PedidoNoModificableException(
                    "El pedido " + idPedido + " esta en " + pedido.getEstado() + " y ya no puede modificarse");
        }
        ItemPedido item = mapperIn.toDomain(dto);
        completarItem(item);
        pedido.agregarItem(item);
        log.info("Item '{}' agregado al pedido {}", item.getNombrePlato(), idPedido);
        return mapperOut.toDTO(pedido);
    }

    @Override
    public List<PedidoResponseDTO> obtenerTodos(String estado) {
        return pedidos.values().stream()
                .filter(p -> estado == null || p.getEstado().name().equalsIgnoreCase(estado))
                .sorted(Comparator.comparing(Pedido::getTimestamp))
                .map(mapperOut::toDTO)
                .toList();
    }

    @Override
    public PedidoResponseDTO obtenerPorId(Long id) {
        return mapperOut.toDTO(buscarOLanzar(id));
    }

    @Override
    public List<PedidoResponseDTO> obtenerActivosPorMesa(Long idMesa) {
        mesaService.obtenerEntidad(idMesa);
        return pedidos.values().stream()
                .filter(p -> p.getIdMesa().equals(idMesa))
                .filter(Pedido::estaActivo)
                .sorted(Comparator.comparing(Pedido::getTimestamp))
                .map(mapperOut::toDTO)
                .toList();
    }

    @Override
    public List<PedidoResponseDTO> obtenerTableroCocina() {
        return pedidos.values().stream()
                .filter(p -> p.getEstado() == EstadoPedido.RECIBIDO
                        || p.getEstado() == EstadoPedido.EN_PREPARACION)
                .sorted(Comparator.comparing(Pedido::getTimestamp))
                .map(mapperOut::toDTO)
                .toList();
    }

    @Override
    public PedidoResponseDTO cambiarEstado(Long id, CambioEstadoRequestDTO dto) {
        Pedido pedido = buscarOLanzar(id);
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(dto.estado());

        if (!pedido.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw new EstadoInvalidoException(
                    "Transicion no permitida: " + pedido.getEstado() + " -> " + nuevoEstado);
        }
        if (nuevoEstado == EstadoPedido.EN_PREPARACION) {
            validarCapacidadParrilla(pedido);
        }

        EstadoPedido anterior = pedido.getEstado();
        pedido.cambiarEstado(nuevoEstado);
        log.info("Pedido {}: {} -> {}", id, anterior, nuevoEstado);
        return mapperOut.toDTO(pedido);
    }

    @Override
    public void cancelar(Long id) {
        Pedido pedido = buscarOLanzar(id);
        if (!pedido.puedeModificarse()) {
            throw new PedidoNoModificableException(
                    "Un pedido en estado " + pedido.getEstado() + " no puede cancelarse");
        }
        pedido.cambiarEstado(EstadoPedido.CANCELADO);
        log.info("Pedido {} cancelado", id);
    }

    @Override
    public List<Pedido> obtenerEntidades() {
        return List.copyOf(pedidos.values());
    }

    @Override
    public List<Pedido> obtenerEntidadesPorMesaDesde(Long idMesa, LocalDateTime desde) {
        return pedidos.values().stream()
                .filter(p -> p.getIdMesa().equals(idMesa))
                .filter(p -> !p.getTimestamp().isBefore(desde))
                .toList();
    }

    private void completarItem(ItemPedido item) {
        Plato plato = platoService.obtenerEntidad(item.getIdPlato());

        if (!plato.estaDisponible()) {
            throw new PlatoNoDisponibleException("El plato '" + plato.getNombre() + "' esta agotado");
        }
        if (plato.esCorte()) {
            if (item.getTerminoCoccion() == null) {
                throw new TerminoCoccionRequeridoException(
                        "El corte '" + plato.getNombre() + "' requiere termino de coccion (AZUL, TRES_CUARTOS, BIEN_ASADO)");
            }
            validarHorarioCorte(plato);
        } else {
            item.setTerminoCoccion(null);
        }

        item.setId(secuenciaItems.incrementAndGet());
        item.setNombrePlato(plato.getNombre());
        item.setCategoria(plato.getCategoria());
        item.setPrecioCongelado(plato.getPrecio());
    }

    private void validarHorarioCorte(Plato plato) {
        if (plato.getTiempoPreparacionMin() == null
                || plato.getTiempoPreparacionMin() <= propiedades.minutosCorteLento()) {
            return;
        }
        LocalTime ahora = LocalTime.now(clock);
        LocalTime cierre = propiedades.horaCierre();
        LocalTime inicioRestriccion = cierre.minusMinutes(propiedades.minutosRestriccionCierre());
        if (!ahora.isBefore(inicioRestriccion) && ahora.isBefore(cierre)) {
            throw new CorteFueraDeHorarioException(
                    "'" + plato.getNombre() + "' tarda " + plato.getTiempoPreparacionMin()
                            + " min y no puede pedirse despues de las " + inicioRestriccion);
        }
    }

    private void validarCapacidadParrilla(Pedido pedido) {
        int cortesEnParrilla = pedidos.values().stream()
                .filter(p -> p.getEstado() == EstadoPedido.EN_PREPARACION)
                .mapToInt(Pedido::contarCortes)
                .sum();
        int cortesNuevos = pedido.contarCortes();
        int capacidad = propiedades.capacidadParrilla();

        if (cortesEnParrilla + cortesNuevos > capacidad) {
            throw new CapacidadParrillaExcedidaException(
                    "Parrilla llena (" + cortesEnParrilla + "/" + capacidad + " cortes). El pedido "
                            + pedido.getId() + " necesita " + cortesNuevos + " y queda en cola en RECIBIDO");
        }
    }

    private Pedido buscarOLanzar(Long id) {
        return Optional.ofNullable(pedidos.get(id))
                .orElseThrow(() -> new PedidoNoEncontradoException("Pedido no encontrado: " + id));
    }
}