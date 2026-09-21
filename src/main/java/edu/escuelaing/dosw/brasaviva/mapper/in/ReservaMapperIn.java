package edu.escuelaing.dosw.brasaviva.mapper.in;

import edu.escuelaing.dosw.brasaviva.dto.request.ReservaRequestDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservaMapperIn {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    Reserva toDomain(ReservaRequestDTO dto);
}