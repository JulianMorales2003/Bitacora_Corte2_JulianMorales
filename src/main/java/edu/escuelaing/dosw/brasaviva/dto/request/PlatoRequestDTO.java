package edu.escuelaing.dosw.brasaviva.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PlatoRequestDTO(

        @NotBlank(message = "El nombre del plato es obligatorio")
        @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
        String nombre,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
        Double precio,

        @NotBlank(message = "La categoria es obligatoria")
        @Pattern(
                regexp = "ENTRADA|CORTE|PLATO_FUERTE|ACOMPANAMIENTO|POSTRE|BEBIDA",
                message = "Categoria invalida. Opciones: ENTRADA, CORTE, PLATO_FUERTE, ACOMPANAMIENTO, POSTRE, BEBIDA"
        )
        String categoria,

        @Size(max = 500, message = "La descripcion no puede superar 500 caracteres")
        String descripcion,

        @NotNull(message = "El tiempo de preparacion es obligatorio")
        @Min(value = 1, message = "El tiempo de preparacion debe ser al menos 1 minuto")
        @Max(value = 120, message = "El tiempo de preparacion no puede superar 120 minutos")
        Integer tiempoPreparacionMin

) {}