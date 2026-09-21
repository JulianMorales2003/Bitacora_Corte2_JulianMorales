package edu.escuelaing.dosw.brasaviva.dto.response;

import java.time.LocalDateTime;

public record ReservaResponseDTO(
        Long id,
        Long idMesa,
        String cliente,
        LocalDateTime fechaHora,
        Integer comensales,
        String estado
) {}