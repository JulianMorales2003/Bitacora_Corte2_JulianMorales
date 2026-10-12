package edu.escuelaing.dosw.brasaviva.persistence.jpa;

import edu.escuelaing.dosw.brasaviva.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repositorio Spring Data sobre UsuarioEntity. Los metodos derivados evitan inyeccion SQL (OWASP A03). */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
