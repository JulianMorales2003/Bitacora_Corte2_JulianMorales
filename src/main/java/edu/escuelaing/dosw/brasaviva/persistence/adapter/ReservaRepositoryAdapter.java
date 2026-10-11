package edu.escuelaing.dosw.brasaviva.persistence.adapter;

import edu.escuelaing.dosw.brasaviva.model.domain.Reserva;
import edu.escuelaing.dosw.brasaviva.persistence.entity.ReservaEntity;
import edu.escuelaing.dosw.brasaviva.persistence.jpa.ReservaJpaRepository;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.ReservaEntityMapper;
import edu.escuelaing.dosw.brasaviva.repository.ReservaRepository;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoReserva;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * Implementa el contrato de dominio {@link ReservaRepository} sobre Spring Data JPA:
 * convierte dominio a entidad al guardar y entidad a dominio al leer.
 */
@Repository
@RequiredArgsConstructor
public class ReservaRepositoryAdapter implements ReservaRepository {

    private final ReservaJpaRepository jpa;
    private final ReservaEntityMapper mapper;

    @Override
    public <S extends Reserva> S save(S reserva) {
        ReservaEntity guardada = jpa.save(mapper.toEntity(reserva));
        reserva.setId(guardada.getId());
        return reserva;
    }

    @Override
    public Optional<Reserva> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Reserva> findAll() {
        return aDominio(jpa.findAll(Sort.by("id")));
    }

    @Override
    public void delete(Reserva reserva) {
        jpa.deleteById(reserva.getId());
    }

    @Override
    public List<Reserva> findByIdMesaAndEstado(Long idMesa, EstadoReserva estado) {
        return aDominio(jpa.findByIdMesaAndEstado(idMesa, estado));
    }

    private List<Reserva> aDominio(List<ReservaEntity> entidades) {
        return entidades.stream().map(mapper::toDomain).toList();
    }
}
