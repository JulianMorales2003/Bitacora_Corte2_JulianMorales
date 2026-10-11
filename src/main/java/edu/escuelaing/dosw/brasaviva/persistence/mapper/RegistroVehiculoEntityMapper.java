package edu.escuelaing.dosw.brasaviva.persistence.mapper;

import edu.escuelaing.dosw.brasaviva.model.domain.RegistroVehiculo;
import edu.escuelaing.dosw.brasaviva.persistence.entity.RegistroVehiculoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** Traduce entre el modelo de dominio y la entidad de persistencia de RegistroVehiculo. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegistroVehiculoEntityMapper {

    RegistroVehiculo toDomain(RegistroVehiculoEntity entity);

    RegistroVehiculoEntity toEntity(RegistroVehiculo domain);
}
