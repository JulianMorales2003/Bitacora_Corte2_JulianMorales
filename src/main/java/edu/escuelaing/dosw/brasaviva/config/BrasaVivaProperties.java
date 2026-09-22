package edu.escuelaing.dosw.brasaviva.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@ConfigurationProperties(prefix = "brasaviva")
public record BrasaVivaProperties(
        int capacidadParrilla,
        @DateTimeFormat(pattern = "HH:mm") LocalTime horaCierre,
        int minutosRestriccionCierre,
        int minutosCorteLento,
        int capacidadParqueadero,
        double tarifaParqueaderoHora
) {}