package edu.escuelaing.dosw.brasaviva.persistence.jpa;

import edu.escuelaing.dosw.brasaviva.persistence.entity.MesaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data sobre la entidad MesaEntity. */
public interface MesaJpaRepository extends JpaRepository<MesaEntity, Long> {
    boolean existsByNumero(Integer numero);

    long countByCuentaAbiertaTrue();
}
