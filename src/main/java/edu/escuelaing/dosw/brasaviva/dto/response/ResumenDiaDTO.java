package edu.escuelaing.dosw.brasaviva.dto.response;

import java.util.Map;

public record ResumenDiaDTO(
        long totalPedidos,
        double ingresoTotal,
        Map<String, Long> platosMasPedidos,
        Map<String, Long> pedidosPorEstado,
        long mesasConCuentaAbierta
) {}