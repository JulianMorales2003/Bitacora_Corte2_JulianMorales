package edu.escuelaing.dosw.brasaviva.persistence.mapper;

import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import edu.escuelaing.dosw.brasaviva.persistence.entity.PlatoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** Traduce entre el modelo de dominio y la entidad de persistencia de Plato. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlatoEntityMapper {

    Plato toDomain(PlatoEntity entity);

    PlatoEntity toEntity(Plato domain);
}
