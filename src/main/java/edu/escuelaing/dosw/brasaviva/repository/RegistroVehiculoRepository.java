package edu.escuelaing.dosw.brasaviva.repository;

import edu.escuelaing.dosw.brasaviva.model.domain.RegistroVehiculo;
import java.util.List;
import java.util.Optional;

public interface RegistroVehiculoRepository extends BaseRepository<RegistroVehiculo, Long> {
    Optional<RegistroVehiculo> findFirstByPlacaAndSalidaIsNull(String placa);

    long countBySalidaIsNull();

    List<RegistroVehiculo> findBySalidaIsNullOrderByEntradaAsc();
}
