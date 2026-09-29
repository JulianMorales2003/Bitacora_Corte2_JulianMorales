package edu.escuelaing.dosw.brasaviva.service.impl;

import edu.escuelaing.dosw.brasaviva.dto.request.AbrirCuentaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PagoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.CuentaResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.CuentaConPedidosPendientesException;
import edu.escuelaing.dosw.brasaviva.exception.CuentaNoEncontradaException;
import edu.escuelaing.dosw.brasaviva.exception.EstadoInvalidoException;
import edu.escuelaing.dosw.brasaviva.exception.PagoInsuficienteException;
import edu.escuelaing.dosw.brasaviva.mapper.out.CuentaMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.Cuenta;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoCuenta;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.MedioPago;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.service.ICuentaService;
import edu.escuelaing.dosw.brasaviva.service.IMesaService;
import edu.escuelaing.dosw.brasaviva.service.IPedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.escuelaing.dosw.brasaviva.repository.CuentaRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CuentaServiceImpl implements ICuentaService {

    private final CuentaRepository cuentaRepository;
    private final IMesaService mesaService;
    private final IPedidoService pedidoService;
    private final CuentaMapperOut mapperOut;
    private final Clock clock;


    @Override
    public CuentaResponseDTO abrir(AbrirCuentaRequestDTO dto) {
        mesaService.abrirCuenta(dto.idMesa());

        Cuenta cuenta = Cuenta.builder()
                .idMesa(dto.idMesa())
                .total(0.0)
                .estado(EstadoCuenta.ABIERTA)
                .fechaApertura(LocalDateTime.now(clock))
                .build();
        cuenta = cuentaRepository.save(cuenta);
        log.info("Cuenta {} abierta para la mesa id={}", cuenta.getId(), dto.idMesa());
        return mapperOut.toDTO(cuenta);
    }

    @Override
    public CuentaResponseDTO obtenerPorId(Long id) {
        Cuenta cuenta = buscarOLanzar(id);
        if (cuenta.estaAbierta()) {
            cuenta.calcularTotal(pedidosDe(cuenta));
            cuenta = cuentaRepository.save(cuenta);
        }
        return mapperOut.toDTO(cuenta);
    }

    @Override
    public CuentaResponseDTO obtenerAbiertaPorMesa(Long idMesa) {
        mesaService.obtenerEntidad(idMesa);
        Cuenta cuenta = cuentaRepository.findFirstByIdMesaAndEstado(idMesa, EstadoCuenta.ABIERTA)
                .orElseThrow(() -> new CuentaNoEncontradaException("La mesa " + idMesa + " no tiene cuenta abierta"));
        cuenta.calcularTotal(pedidosDe(cuenta));
        cuenta = cuentaRepository.save(cuenta);
        return mapperOut.toDTO(cuenta);
    }

    @Override
    public CuentaResponseDTO registrarPago(Long idCuenta, PagoRequestDTO dto) {
        Cuenta cuenta = buscarOLanzar(idCuenta);
        if (!cuenta.estaAbierta()) {
            throw new EstadoInvalidoException("La cuenta " + idCuenta + " ya esta " + cuenta.getEstado());
        }

        List<Pedido> pedidos = pedidosDe(cuenta);
        long pendientes = pedidos.stream()
                .filter(p -> p.getEstado() != EstadoPedido.ENTREGADO && p.getEstado() != EstadoPedido.CANCELADO)
                .count();
        if (pendientes > 0) {
            throw new CuentaConPedidosPendientesException(
                    "La cuenta tiene " + pendientes + " pedido(s) sin entregar");
        }

        double total = cuenta.calcularTotal(pedidos);
        if (dto.montoRecibido() < total) {
            throw new PagoInsuficienteException(
                    "El monto recibido (" + dto.montoRecibido() + ") no cubre el total (" + total + ")");
        }

        cuenta.registrarPago(MedioPago.valueOf(dto.medioPago()), dto.montoRecibido());
        cuenta = cuentaRepository.save(cuenta);
        mesaService.cerrarCuenta(cuenta.getIdMesa());
        log.info("Cuenta {} pagada con {}: total={}, cambio={}",
                idCuenta, dto.medioPago(), total, cuenta.getCambio());
        return mapperOut.toDTO(cuenta);
    }

    private List<Pedido> pedidosDe(Cuenta cuenta) {
        return pedidoService.obtenerEntidadesPorMesaDesde(cuenta.getIdMesa(), cuenta.getFechaApertura());
    }

    private Cuenta buscarOLanzar(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada: " + id));
    }
}