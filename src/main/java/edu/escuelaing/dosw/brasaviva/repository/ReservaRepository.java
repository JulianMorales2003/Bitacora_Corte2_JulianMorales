package edu.escuelaing.dosw.brasaviva.repository;

import edu.escuelaing.dosw.brasaviva.model.domain.Reserva;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoReserva;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByIdMesaAndEstado(Long idMesa, EstadoReserva estado);

}
