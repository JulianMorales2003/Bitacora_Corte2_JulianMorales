package edu.escuelaing.dosw.brasaviva.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReservaRequestDTO(

        @NotNull(message = "El id de la mesa es obligatorio")
        Long idMesa,

        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(min = 2, max = 80, message = "El nombre del cliente debe tener entre 2 y 80 caracteres")
        String cliente,

        @NotNull(message = "La fecha y hora son obligatorias")
        @Future(message = "La reserva debe ser en una fecha futura")
        LocalDateTime fechaHora,

        @NotNull(message = "El numero de comensales es obligatorio")
        @Min(value = 1, message = "Debe haber al menos 1 comensal")
        @Max(value = 20, message = "Maximo 20 comensales por reserva")
        Integer comensales

) {}