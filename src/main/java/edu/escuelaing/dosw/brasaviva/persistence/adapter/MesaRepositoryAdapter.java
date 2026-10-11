package edu.escuelaing.dosw.brasaviva.persistence.adapter;

import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import edu.escuelaing.dosw.brasaviva.persistence.entity.MesaEntity;
import edu.escuelaing.dosw.brasaviva.persistence.jpa.MesaJpaRepository;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.MesaEntityMapper;
import edu.escuelaing.dosw.brasaviva.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * Implementa el contrato de dominio {@link MesaRepository} sobre Spring Data JPA:
 * convierte dominio a entidad al guardar y entidad a dominio al leer.
 */
@Repository
@RequiredArgsConstructor
public class MesaRepositoryAdapter implements MesaRepository {

    private final MesaJpaRepository jpa;
    private final MesaEntityMapper mapper;

    @Override
    public <S extends Mesa> S save(S mesa) {
        MesaEntity guardada = jpa.save(mapper.toEntity(mesa));
        mesa.setId(guardada.getId());
        return mesa;
    }

    @Override
    public Optional<Mesa> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Mesa> findAll() {
        return aDominio(jpa.findAll(Sort.by("id")));
    }

    @Override
    public void delete(Mesa mesa) {
        jpa.deleteById(mesa.getId());
    }

    @Override
    public boolean existsByNumero(Integer numero) {
        return jpa.existsByNumero(numero);
    }

    @Override
    public long countByCuentaAbiertaTrue() {
        return jpa.countByCuentaAbiertaTrue();
    }

    private List<Mesa> aDominio(List<MesaEntity> entidades) {
        return entidades.stream().map(mapper::toDomain).toList();
    }
}
