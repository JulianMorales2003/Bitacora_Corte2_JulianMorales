package edu.escuelaing.dosw.brasaviva.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EntradaVehiculoRequestDTO(

        // Carro: ABC123 - Moto: ABC12D
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^[A-Za-z]{3}[0-9]{2}[0-9A-Za-z]$",
                message = "Placa invalida. Formato: ABC123 (carro) o ABC12D (moto)")
        String placa

) {}