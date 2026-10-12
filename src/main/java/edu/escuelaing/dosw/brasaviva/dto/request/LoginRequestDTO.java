package edu.escuelaing.dosw.brasaviva.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(

        @NotBlank(message = "El email es obligatorio")
        @Size(max = 150, message = "El email no puede superar 150 caracteres")
        String email,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(max = 72, message = "La contrasena no puede superar 72 caracteres")
        String password

) {}
