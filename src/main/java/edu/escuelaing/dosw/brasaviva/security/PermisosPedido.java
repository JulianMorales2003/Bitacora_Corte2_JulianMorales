package edu.escuelaing.dosw.brasaviva.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Regla de la matriz de roles que depende del estado destino y no solo de la URL
 * (README, "Roles y permisos"):
 * <ul>
 *   <li>EN_PREPARACION / LISTO: solo PARRILLERO</li>
 *   <li>ENTREGADO: solo MESERO</li>
 *   <li>CANCELADO: MESERO o ADMIN</li>
 * </ul>
 * Se usa desde {@code @PreAuthorize("@permisosPedido.puedeCambiarA(#dto.estado(), authentication)")}.
 */
@Component("permisosPedido")
public class PermisosPedido {

    public boolean puedeCambiarA(String estado, Authentication auth) {
        if (estado == null || auth == null || !auth.isAuthenticated()) {
            return false;
        }
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return switch (estado) {
            case "EN_PREPARACION", "LISTO" -> roles.contains("ROLE_PARRILLERO");
            case "ENTREGADO" -> roles.contains("ROLE_MESERO");
            case "CANCELADO" -> roles.contains("ROLE_MESERO") || roles.contains("ROLE_ADMIN");
            default -> false;
        };
    }
}
