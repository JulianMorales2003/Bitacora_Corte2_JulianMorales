package edu.escuelaing.dosw.brasaviva.persistence.adapter;

import edu.escuelaing.dosw.brasaviva.model.domain.RegistroVehiculo;
import edu.escuelaing.dosw.brasaviva.persistence.entity.RegistroVehiculoEntity;
import edu.escuelaing.dosw.brasaviva.persistence.jpa.RegistroVehiculoJpaRepository;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.RegistroVehiculoEntityMapper;
import edu.escuelaing.dosw.brasaviva.repository.RegistroVehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * Implementa el contrato de dominio {@link RegistroVehiculoRepository} sobre Spring Data JPA:
 * convierte dominio a entidad al guardar y entidad a dominio al leer.
 */
@Repository
@RequiredArgsConstructor
public class RegistroVehiculoRepositoryAdapter implements RegistroVehiculoRepository {

    private final RegistroVehiculoJpaRepository jpa;
    private final RegistroVehiculoEntityMapper mapper;

    @Override
    public <S extends RegistroVehiculo> S save(S registroVehiculo) {
        RegistroVehiculoEntity guardada = jpa.save(mapper.toEntity(registroVehiculo));
        registroVehiculo.setId(guardada.getId());
        return registroVehiculo;
    }

    @Override
    public Optional<RegistroVehiculo> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<RegistroVehiculo> findAll() {
        return aDominio(jpa.findAll(Sort.by("id")));
    }

    @Override
    public void delete(RegistroVehiculo registroVehiculo) {
        jpa.deleteById(registroVehiculo.getId());
    }

    @Override
    public Optional<RegistroVehiculo> findFirstByPlacaAndSalidaIsNull(String placa) {
        return jpa.findFirstByPlacaAndSalidaIsNull(placa).map(mapper::toDomain);
    }

    @Override
    public long countBySalidaIsNull() {
        return jpa.countBySalidaIsNull();
    }

    @Override
    public List<RegistroVehiculo> findBySalidaIsNullOrderByEntradaAsc() {
        return aDominio(jpa.findBySalidaIsNullOrderByEntradaAsc());
    }

    private List<RegistroVehiculo> aDominio(List<RegistroVehiculoEntity> entidades) {
        return entidades.stream().map(mapper::toDomain).toList();
    }
}
