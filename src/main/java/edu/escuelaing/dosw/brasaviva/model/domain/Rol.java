package edu.escuelaing.dosw.brasaviva.model.domain;

/**
 * Roles del restaurante (ver matriz de permisos del README).
 * Spring Security les antepone el prefijo {@code ROLE_} automaticamente.
 */
public enum Rol {
    CLIENTE,
    MESERO,
    PARRILLERO,
    ADMIN
}
