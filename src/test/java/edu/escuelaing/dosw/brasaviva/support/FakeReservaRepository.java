package edu.escuelaing.dosw.brasaviva.support;

import edu.escuelaing.dosw.brasaviva.model.domain.EstadoReserva;
import edu.escuelaing.dosw.brasaviva.model.domain.Reserva;
import edu.escuelaing.dosw.brasaviva.repository.ReservaRepository;

import java.util.List;

public class FakeReservaRepository extends FakeJpaRepository<Reserva, Long> implements ReservaRepository {

    public FakeReservaRepository() {
        super(Reserva::getId, Reserva::setId);
    }

    @Override
    public List<Reserva> findByIdMesaAndEstado(Long idMesa, EstadoReserva estado) {
        return valoresOrdenados().stream()
                .filter(r -> r.getIdMesa().equals(idMesa) && r.getEstado() == estado)
                .toList();
    }
}
