package edu.escuelaing.dosw.brasaviva.dto.response;

import edu.escuelaing.dosw.brasaviva.model.domain.Rol;

/** Vista publica de un usuario. Deliberadamente NO incluye el password ni su hash (OWASP A04). */
public record UsuarioResponseDTO(
        Long id,
        String nombre,
        String email,
        Rol rol
) {}
