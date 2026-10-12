package edu.escuelaing.dosw.brasaviva.dto.response;

/** Respuesta del login: el JWT que el cliente envia luego como {@code Authorization: Bearer <token>}. */
public record TokenResponseDTO(
        String token,
        String tipo,
        long expiraEnMs,
        String email,
        String rol
) {}
