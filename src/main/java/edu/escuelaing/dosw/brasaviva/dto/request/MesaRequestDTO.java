package edu.escuelaing.dosw.brasaviva.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MesaRequestDTO(

        @NotNull(message = "El numero de mesa es obligatorio")
        @Min(value = 1, message = "El numero de mesa debe ser positivo")
        Integer numero,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad minima es 1 persona")
        @Max(value = 20, message = "La capacidad maxima es 20 personas")
        Integer capacidad

) {}