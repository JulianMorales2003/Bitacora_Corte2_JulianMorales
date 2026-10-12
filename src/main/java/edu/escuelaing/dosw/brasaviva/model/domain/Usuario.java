package edu.escuelaing.dosw.brasaviva.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Usuario del sistema. El password siempre se guarda hasheado con BCrypt. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long id;
    private String nombre;
    private String email;
    @ToString.Exclude
    private String password;
    private Rol rol;
}
