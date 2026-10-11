package edu.escuelaing.dosw.brasaviva.persistence.jpa;

import edu.escuelaing.dosw.brasaviva.persistence.entity.ReservaEntity;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoReserva;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data sobre la entidad ReservaEntity. */
public interface ReservaJpaRepository extends JpaRepository<ReservaEntity, Long> {
    List<ReservaEntity> findByIdMesaAndEstado(Long idMesa, EstadoReserva estado);
}
