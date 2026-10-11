package edu.escuelaing.dosw.brasaviva.persistence.adapter;

import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import edu.escuelaing.dosw.brasaviva.persistence.entity.PlatoEntity;
import edu.escuelaing.dosw.brasaviva.persistence.jpa.PlatoJpaRepository;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.PlatoEntityMapper;
import edu.escuelaing.dosw.brasaviva.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * Implementa el contrato de dominio {@link PlatoRepository} sobre Spring Data JPA:
 * convierte dominio a entidad al guardar y entidad a dominio al leer.
 */
@Repository
@RequiredArgsConstructor
public class PlatoRepositoryAdapter implements PlatoRepository {

    private final PlatoJpaRepository jpa;
    private final PlatoEntityMapper mapper;

    @Override
    public <S extends Plato> S save(S plato) {
        PlatoEntity guardada = jpa.save(mapper.toEntity(plato));
        plato.setId(guardada.getId());
        return plato;
    }

    @Override
    public Optional<Plato> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Plato> findAll() {
        return aDominio(jpa.findAll(Sort.by("id")));
    }

    @Override
    public void delete(Plato plato) {
        jpa.deleteById(plato.getId());
    }

    @Override
    public boolean existsByNombreIgnoreCase(String nombre) {
        return jpa.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id) {
        return jpa.existsByNombreIgnoreCaseAndIdNot(nombre, id);
    }

    @Override
    public List<Plato> findByDisponibleTrue() {
        return aDominio(jpa.findByDisponibleTrue());
    }

    private List<Plato> aDominio(List<PlatoEntity> entidades) {
        return entidades.stream().map(mapper::toDomain).toList();
    }
}
