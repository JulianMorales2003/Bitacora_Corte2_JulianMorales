package edu.escuelaing.dosw.brasaviva.dto.response;

public record PlatoResponseDTO(
        Long id,
        String nombre,
        Double precio,
        String categoria,
        String descripcion,
        Boolean disponible,
        Integer tiempoPreparacionMin
) {}