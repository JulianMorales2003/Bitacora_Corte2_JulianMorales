package edu.escuelaing.dosw.brasaviva.persistence.jpa;

import edu.escuelaing.dosw.brasaviva.persistence.entity.CuentaEntity;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoCuenta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data sobre la entidad CuentaEntity. */
public interface CuentaJpaRepository extends JpaRepository<CuentaEntity, Long> {
    Optional<CuentaEntity> findFirstByIdMesaAndEstado(Long idMesa, EstadoCuenta estado);
}
