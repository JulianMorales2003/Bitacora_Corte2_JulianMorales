package edu.escuelaing.dosw.brasaviva.dto.request;

import jakarta.validation.constraints.NotNull;

public record DisponibilidadRequestDTO(

        @NotNull(message = "Debe indicar si el plato esta disponible")
        Boolean disponible

) {}