package edu.escuelaing.dosw.brasaviva.persistence.jpa;

import edu.escuelaing.dosw.brasaviva.persistence.entity.PlatoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data sobre la entidad PlatoEntity. */
public interface PlatoJpaRepository extends JpaRepository<PlatoEntity, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<PlatoEntity> findByDisponibleTrue();
}
