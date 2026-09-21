package edu.escuelaing.dosw.brasaviva.dto.response;

public record ItemPedidoResponseDTO(
        Long id,
        Long idPlato,
        String nombrePlato,
        Double precioCongelado,
        Integer cantidad,
        String terminoCoccion,
        String observaciones,
        Double subtotal
) {}