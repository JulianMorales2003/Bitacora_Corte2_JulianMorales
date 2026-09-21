package edu.escuelaing.dosw.brasaviva.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoRequestDTO(

        @NotNull(message = "El id de la mesa es obligatorio")
        Long idMesa,

        @NotEmpty(message = "El pedido debe tener al menos un plato")
        List<@Valid ItemPedidoRequestDTO> items

) {}