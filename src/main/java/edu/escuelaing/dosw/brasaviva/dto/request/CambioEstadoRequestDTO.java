package edu.escuelaing.dosw.brasaviva.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambioEstadoRequestDTO(

        @NotBlank(message = "El nuevo estado es obligatorio")
        @Pattern(regexp = "RECIBIDO|EN_PREPARACION|LISTO|ENTREGADO|CANCELADO",
                message = "Estado invalido. Opciones: RECIBIDO, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO")
        String estado

) {}