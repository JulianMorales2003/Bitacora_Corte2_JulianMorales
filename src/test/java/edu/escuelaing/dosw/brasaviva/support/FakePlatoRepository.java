package edu.escuelaing.dosw.brasaviva.support;

import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import edu.escuelaing.dosw.brasaviva.repository.PlatoRepository;

import java.util.List;

public class FakePlatoRepository extends FakeJpaRepository<Plato, Long> implements PlatoRepository {

    public FakePlatoRepository() {
        super(Plato::getId, Plato::setId);
    }

    @Override
    public boolean existsByNombreIgnoreCase(String nombre) {
        return datos.values().stream().anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));
    }

    @Override
    public boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id) {
        return datos.values().stream()
                .anyMatch(p -> !p.getId().equals(id) && p.getNombre().equalsIgnoreCase(nombre));
    }

    @Override
    public List<Plato> findByDisponibleTrue() {
        return valoresOrdenados().stream().filter(Plato::estaDisponible).toList();
    }
}
