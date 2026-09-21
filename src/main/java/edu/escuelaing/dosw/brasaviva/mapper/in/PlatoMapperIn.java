package edu.escuelaing.dosw.brasaviva.mapper.in;

import edu.escuelaing.dosw.brasaviva.dto.request.PlatoRequestDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlatoMapperIn {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disponible", ignore = true)
    Plato toDomain(PlatoRequestDTO dto);
}