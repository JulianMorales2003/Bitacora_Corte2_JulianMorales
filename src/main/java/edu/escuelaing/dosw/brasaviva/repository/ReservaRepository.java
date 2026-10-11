package edu.escuelaing.dosw.brasaviva.repository;

import edu.escuelaing.dosw.brasaviva.model.domain.Reserva;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoReserva;
import java.util.List;

public interface ReservaRepository extends BaseRepository<Reserva, Long> {
    List<Reserva> findByIdMesaAndEstado(Long idMesa, EstadoReserva estado);
}
