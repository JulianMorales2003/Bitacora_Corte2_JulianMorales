package edu.escuelaing.dosw.brasaviva.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ItemPedidoRequestDTO(

        @NotNull(message = "El id del plato es obligatorio")
        Long idPlato,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad minima es 1")
        @Max(value = 20, message = "La cantidad maxima por item es 20")
        Integer cantidad,


        @Pattern(regexp = "AZUL|TRES_CUARTOS|BIEN_ASADO",
                message = "Termino invalido. Opciones: AZUL, TRES_CUARTOS, BIEN_ASADO")
        String terminoCoccion,

        @Size(max = 200, message = "Las observaciones no pueden superar 200 caracteres")
        String observaciones

) {}