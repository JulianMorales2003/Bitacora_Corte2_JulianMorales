package edu.escuelaing.dosw.brasaviva.repository;

import edu.escuelaing.dosw.brasaviva.model.domain.Cuenta;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoCuenta;
import java.util.Optional;

public interface CuentaRepository extends BaseRepository<Cuenta, Long> {
    Optional<Cuenta> findFirstByIdMesaAndEstado(Long idMesa, EstadoCuenta estado);
}
