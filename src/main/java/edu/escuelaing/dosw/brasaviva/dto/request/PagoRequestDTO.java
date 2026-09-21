package edu.escuelaing.dosw.brasaviva.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PagoRequestDTO(

        @NotBlank(message = "El medio de pago es obligatorio")
        @Pattern(regexp = "EFECTIVO|TARJETA", message = "Medio de pago invalido. Opciones: EFECTIVO, TARJETA")
        String medioPago,

        @NotNull(message = "El monto recibido es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        Double montoRecibido

) {}