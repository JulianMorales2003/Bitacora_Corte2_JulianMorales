package edu.escuelaing.dosw.brasaviva.mapper.in;

import edu.escuelaing.dosw.brasaviva.dto.request.MesaRequestDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MesaMapperIn {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "cuentaAbierta", ignore = true)
    Mesa toDomain(MesaRequestDTO dto);
}