package edu.escuelaing.dosw.brasaviva.dto.response;

/**
 * Vista del cliente: no expone disponibilidad ni tiempos internos de cocina.
 */
public record MenuItemResponseDTO(
        Long id,
        String nombre,
        Double precio,
        String categoria,
        String descripcion
) {}