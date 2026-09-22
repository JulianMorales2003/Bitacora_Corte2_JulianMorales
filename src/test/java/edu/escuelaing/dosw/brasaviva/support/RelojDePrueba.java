package edu.escuelaing.dosw.brasaviva.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class RelojDePrueba extends Clock {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private Instant instante;

    public RelojDePrueba(LocalDateTime fechaHora) {
        this.instante = fechaHora.atZone(ZONA).toInstant();
    }

    public void adelantar(Duration duracion) {
        instante = instante.plus(duracion);
    }

    public void fijar(LocalDateTime fechaHora) {
        instante = fechaHora.atZone(ZONA).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return ZONA;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instante;
    }
}