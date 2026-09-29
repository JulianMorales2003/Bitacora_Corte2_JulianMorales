package edu.escuelaing.dosw.brasaviva.support;

import edu.escuelaing.dosw.brasaviva.model.domain.Cuenta;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoCuenta;
import edu.escuelaing.dosw.brasaviva.repository.CuentaRepository;

import java.util.Optional;

public class FakeCuentaRepository extends FakeJpaRepository<Cuenta, Long> implements CuentaRepository {

    public FakeCuentaRepository() {
        super(Cuenta::getId, Cuenta::setId);
    }

    @Override
    public Optional<Cuenta> findFirstByIdMesaAndEstado(Long idMesa, EstadoCuenta estado) {
        return valoresOrdenados().stream()
                .filter(c -> c.getIdMesa().equals(idMesa) && c.getEstado() == estado)
                .findFirst();
    }
}
