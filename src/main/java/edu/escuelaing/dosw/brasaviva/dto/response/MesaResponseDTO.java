package edu.escuelaing.dosw.brasaviva.dto.response;

public record MesaResponseDTO(
        Long id,
        Integer numero,
        Integer capacidad,
        String estado,
        Boolean cuentaAbierta
) {}