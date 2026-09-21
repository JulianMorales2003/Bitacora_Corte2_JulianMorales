package edu.escuelaing.dosw.brasaviva.dto.response;

import java.time.LocalDateTime;

public record CuentaResponseDTO(
        Long id,
        Long idMesa,
        Double total,
        String estado,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        String medioPago,
        Double montoRecibido,
        Double cambio
) {}