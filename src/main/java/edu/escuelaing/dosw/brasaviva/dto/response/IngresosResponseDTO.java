package edu.escuelaing.dosw.brasaviva.dto.response;

import java.time.LocalDate;
import java.util.Map;

public record IngresosResponseDTO(
        LocalDate desde,
        LocalDate hasta,
        Map<String, Double> ingresosPorCategoria,
        double total
) {}