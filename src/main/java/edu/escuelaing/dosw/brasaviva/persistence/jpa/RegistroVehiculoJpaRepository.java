package edu.escuelaing.dosw.brasaviva.persistence.jpa;

import edu.escuelaing.dosw.brasaviva.persistence.entity.RegistroVehiculoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data sobre la entidad RegistroVehiculoEntity. */
public interface RegistroVehiculoJpaRepository extends JpaRepository<RegistroVehiculoEntity, Long> {
    Optional<RegistroVehiculoEntity> findFirstByPlacaAndSalidaIsNull(String placa);

    long countBySalidaIsNull();

    List<RegistroVehiculoEntity> findBySalidaIsNullOrderByEntradaAsc();
}
