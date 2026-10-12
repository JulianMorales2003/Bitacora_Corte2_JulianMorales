package edu.escuelaing.dosw.brasaviva.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita los intentos de login fallidos por email (proteccion basica contra fuerza bruta,
 * OWASP A07). Tras {@value #MAX_INTENTOS} fallos en {@link #VENTANA} el login queda bloqueado
 * hasta que termine la ventana. Es en memoria: sirve para una sola instancia de la API.
 */
@Component
public class LoginAttemptService {

    static final int MAX_INTENTOS = 5;
    static final Duration VENTANA = Duration.ofMinutes(15);
    private static final int LIMITE_ENTRADAS = 10_000;

    private record Intentos(int cantidad, Instant primerFallo) {}

    private final Map<String, Intentos> fallos = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public boolean estaBloqueado(String email) {
        Intentos intentos = fallos.get(email);
        if (intentos == null) {
            return false;
        }
        if (vencido(intentos)) {
            fallos.remove(email);
            return false;
        }
        return intentos.cantidad() >= MAX_INTENTOS;
    }

    public void registrarFallo(String email) {
        if (fallos.size() > LIMITE_ENTRADAS) {
            fallos.values().removeIf(this::vencido); // evita crecimiento sin limite
        }
        Instant ahora = clock.instant();
        fallos.merge(email, new Intentos(1, ahora),
                (actual, nuevo) -> vencido(actual) ? nuevo : new Intentos(actual.cantidad() + 1, actual.primerFallo()));
    }

    public void registrarExito(String email) {
        fallos.remove(email);
    }

    private boolean vencido(Intentos intentos) {
        return clock.instant().isAfter(intentos.primerFallo().plus(VENTANA));
    }
}
