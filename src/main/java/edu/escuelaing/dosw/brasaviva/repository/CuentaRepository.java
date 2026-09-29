package edu.escuelaing.dosw.brasaviva.repository;

import edu.escuelaing.dosw.brasaviva.model.domain.Cuenta;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoCuenta;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findFirstByIdMesaAndEstado(Long idMesa, EstadoCuenta estado);

}
