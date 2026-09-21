package edu.escuelaing.dosw.brasaviva.dto.request;

import jakarta.validation.constraints.NotNull;

public record AbrirCuentaRequestDTO(

        @NotNull(message = "El id de la mesa es obligatorio")
        Long idMesa

) {}