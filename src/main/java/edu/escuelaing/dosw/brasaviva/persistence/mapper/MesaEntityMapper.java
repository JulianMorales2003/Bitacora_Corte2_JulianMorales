package edu.escuelaing.dosw.brasaviva.persistence.mapper;

import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import edu.escuelaing.dosw.brasaviva.persistence.entity.MesaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** Traduce entre el modelo de dominio y la entidad de persistencia de Mesa. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MesaEntityMapper {

    Mesa toDomain(MesaEntity entity);

    MesaEntity toEntity(Mesa domain);
}
