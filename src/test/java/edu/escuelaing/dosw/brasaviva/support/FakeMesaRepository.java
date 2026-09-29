package edu.escuelaing.dosw.brasaviva.support;

import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import edu.escuelaing.dosw.brasaviva.repository.MesaRepository;

public class FakeMesaRepository extends FakeJpaRepository<Mesa, Long> implements MesaRepository {

    public FakeMesaRepository() {
        super(Mesa::getId, Mesa::setId);
    }

    @Override
    public boolean existsByNumero(Integer numero) {
        return datos.values().stream().anyMatch(m -> m.getNumero().equals(numero));
    }

    @Override
    public long countByCuentaAbiertaTrue() {
        return datos.values().stream().filter(Mesa::tieneCuentaAbierta).count();
    }
}
