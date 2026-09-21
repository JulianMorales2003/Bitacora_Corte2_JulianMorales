package edu.escuelaing.dosw.brasaviva.dto.response;

public record DisponibilidadParqueaderoDTO(
        int capacidad,
        long ocupados,
        long disponibles
) {}