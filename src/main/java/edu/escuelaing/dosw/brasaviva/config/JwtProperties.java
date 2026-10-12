package edu.escuelaing.dosw.brasaviva.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion del JWT. El secreto va en Base64 y se inyecta por la variable de entorno
 * {@code JWT_SECRET}; nunca debe subirse al repositorio el valor de produccion.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, long expirationMs) {}
