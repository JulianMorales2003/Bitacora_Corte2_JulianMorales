package edu.escuelaing.dosw.brasaviva.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroVehiculo {

    private Long id;
    private String placa;
    private LocalDateTime entrada;
    private LocalDateTime salida;
    private Double cobro;

    public boolean estaActivo() {
        return salida == null;
    }

    public void registrarSalida(LocalDateTime momento) {
        this.salida = momento;
    }

    public double calcularCobro(double tarifaHora) {
        LocalDateTime fin = salida != null ? salida : LocalDateTime.now();
        long minutos = Duration.between(entrada, fin).toMinutes();
        long horas = Math.max(1, (long) Math.ceil(minutos / 60.0));
        this.cobro = horas * tarifaHora;
        return this.cobro;
    }
}