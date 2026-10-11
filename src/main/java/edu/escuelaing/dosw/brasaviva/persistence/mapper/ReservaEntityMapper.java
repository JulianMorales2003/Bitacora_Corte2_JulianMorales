package edu.escuelaing.dosw.brasaviva.persistence.mapper;

import edu.escuelaing.dosw.brasaviva.model.domain.Reserva;
import edu.escuelaing.dosw.brasaviva.persistence.entity.ReservaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** Traduce entre el modelo de dominio y la entidad de persistencia de Reserva. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservaEntityMapper {

    Reserva toDomain(ReservaEntity entity);

    ReservaEntity toEntity(Reserva domain);
}
