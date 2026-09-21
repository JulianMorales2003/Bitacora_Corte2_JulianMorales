package edu.escuelaing.dosw.brasaviva.dto.response;

import java.time.LocalDateTime;

public record RegistroVehiculoResponseDTO(
        Long id,
        String placa,
        LocalDateTime entrada,
        LocalDateTime salida,
        Double cobro,
        Boolean activo
) {}