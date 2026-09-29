package edu.escuelaing.dosw.brasaviva.support;

import edu.escuelaing.dosw.brasaviva.model.domain.RegistroVehiculo;
import edu.escuelaing.dosw.brasaviva.repository.RegistroVehiculoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FakeRegistroVehiculoRepository extends FakeJpaRepository<RegistroVehiculo, Long>
        implements RegistroVehiculoRepository {

    public FakeRegistroVehiculoRepository() {
        super(RegistroVehiculo::getId, RegistroVehiculo::setId);
    }

    @Override
    public Optional<RegistroVehiculo> findFirstByPlacaAndSalidaIsNull(String placa) {
        return datos.values().stream()
                .filter(r -> r.getSalida() == null && r.getPlaca().equals(placa))
                .findFirst();
    }

    @Override
    public long countBySalidaIsNull() {
        return datos.values().stream().filter(r -> r.getSalida() == null).count();
    }

    @Override
    public List<RegistroVehiculo> findBySalidaIsNullOrderByEntradaAsc() {
        return datos.values().stream()
                .filter(r -> r.getSalida() == null)
                .sorted(Comparator.comparing(RegistroVehiculo::getEntrada))
                .toList();
    }
}
