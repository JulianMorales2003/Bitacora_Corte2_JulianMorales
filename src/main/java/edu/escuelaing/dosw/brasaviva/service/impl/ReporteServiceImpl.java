package edu.escuelaing.dosw.brasaviva.service.impl;

import edu.escuelaing.dosw.brasaviva.dto.response.IngresosResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoPopularDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ResumenDiaDTO;
import edu.escuelaing.dosw.brasaviva.exception.RangoFechasInvalidoException;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.service.IMesaService;
import edu.escuelaing.dosw.brasaviva.service.IPedidoService;
import edu.escuelaing.dosw.brasaviva.service.IReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReporteServiceImpl implements IReporteService {

    private final IPedidoService pedidoService;
    private final IMesaService mesaService;
    private final Clock clock;

    @Override
    public ResumenDiaDTO resumenDelDia() {
        LocalDate hoy = LocalDate.now(clock);
        List<Pedido> pedidosHoy = pedidoService.obtenerEntidades().stream()
                .filter(p -> p.getTimestamp().toLocalDate().equals(hoy))
                .toList();
        List<Pedido> vendidos = pedidosHoy.stream()
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO)
                .toList();

        double ingresoTotal = vendidos.stream()
                .mapToDouble(Pedido::calcularTotal)
                .sum();

        Map<String, Long> platosMasPedidos = vendidos.stream()
                .flatMap(p -> p.getItems().stream())
                .collect(Collectors.groupingBy(
                        ItemPedido::getNombrePlato,
                        TreeMap::new,
                        Collectors.summingLong(ItemPedido::getCantidad)));

        Map<String, Long> pedidosPorEstado = pedidosHoy.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getEstado().name(),
                        TreeMap::new,
                        Collectors.counting()));

        log.info("Resumen del dia {}: {} pedidos, ingreso={}", hoy, vendidos.size(), ingresoTotal);
        return new ResumenDiaDTO(vendidos.size(), ingresoTotal, platosMasPedidos,
                pedidosPorEstado, mesaService.contarConCuentaAbierta());
    }

    @Override
    public List<PlatoPopularDTO> platosPopulares(int top) {
        return pedidoService.obtenerEntidades().stream()
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO)
                .flatMap(p -> p.getItems().stream())
                .collect(Collectors.groupingBy(ItemPedido::getNombrePlato,
                        Collectors.summingLong(ItemPedido::getCantidad)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(top)
                .map(e -> new PlatoPopularDTO(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public IngresosResponseDTO ingresos(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new RangoFechasInvalidoException(
                    "La fecha inicial (" + desde + ") es posterior a la final (" + hasta + ")");
        }
        Map<String, Double> porCategoria = pedidoService.obtenerEntidades().stream()
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO)
                .filter(p -> {
                    LocalDate fecha = p.getTimestamp().toLocalDate();
                    return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
                })
                .flatMap(p -> p.getItems().stream())
                .collect(Collectors.groupingBy(
                        ItemPedido::getCategoria,
                        TreeMap::new,
                        Collectors.summingDouble(ItemPedido::subtotal)));

        double total = porCategoria.values().stream().mapToDouble(Double::doubleValue).sum();
        return new IngresosResponseDTO(desde, hasta, porCategoria, total);
    }
}