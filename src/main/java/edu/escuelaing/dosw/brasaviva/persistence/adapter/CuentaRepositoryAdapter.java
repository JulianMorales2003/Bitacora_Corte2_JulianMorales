package edu.escuelaing.dosw.brasaviva.persistence.adapter;

import edu.escuelaing.dosw.brasaviva.model.domain.Cuenta;
import edu.escuelaing.dosw.brasaviva.persistence.entity.CuentaEntity;
import edu.escuelaing.dosw.brasaviva.persistence.jpa.CuentaJpaRepository;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.CuentaEntityMapper;
import edu.escuelaing.dosw.brasaviva.repository.CuentaRepository;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoCuenta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * Implementa el contrato de dominio {@link CuentaRepository} sobre Spring Data JPA:
 * convierte dominio a entidad al guardar y entidad a dominio al leer.
 */
@Repository
@RequiredArgsConstructor
public class CuentaRepositoryAdapter implements CuentaRepository {

    private final CuentaJpaRepository jpa;
    private final CuentaEntityMapper mapper;

    @Override
    public <S extends Cuenta> S save(S cuenta) {
        CuentaEntity guardada = jpa.save(mapper.toEntity(cuenta));
        cuenta.setId(guardada.getId());
        return cuenta;
    }

    @Override
    public Optional<Cuenta> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Cuenta> findAll() {
        return aDominio(jpa.findAll(Sort.by("id")));
    }

    @Override
    public void delete(Cuenta cuenta) {
        jpa.deleteById(cuenta.getId());
    }

    @Override
    public Optional<Cuenta> findFirstByIdMesaAndEstado(Long idMesa, EstadoCuenta estado) {
        return jpa.findFirstByIdMesaAndEstado(idMesa, estado).map(mapper::toDomain);
    }

    private List<Cuenta> aDominio(List<CuentaEntity> entidades) {
        return entidades.stream().map(mapper::toDomain).toList();
    }
}
