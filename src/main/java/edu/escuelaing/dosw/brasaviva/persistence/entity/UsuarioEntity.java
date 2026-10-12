package edu.escuelaing.dosw.brasaviva.persistence.entity;

import edu.escuelaing.dosw.brasaviva.model.domain.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Tabla {@code usuarios}. Solo describe la tabla; la logica vive en {@code model.domain.Usuario}. */
@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    /** Hash BCrypt (60 caracteres). Nunca texto plano. */
    @ToString.Exclude
    @Column(nullable = false, length = 100)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;
}
