package edu.escuelaing.dosw.brasaviva.repository;

import edu.escuelaing.dosw.brasaviva.model.domain.RegistroVehiculo;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroVehiculoRepository extends JpaRepository<RegistroVehiculo, Long> {
    Optional<RegistroVehiculo> findFirstByPlacaAndSalidaIsNull(String placa);

    long countBySalidaIsNull();

    List<RegistroVehiculo> findBySalidaIsNullOrderByEntradaAsc();

}
