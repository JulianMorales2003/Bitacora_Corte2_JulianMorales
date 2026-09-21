package edu.escuelaing.dosw.brasaviva.mapper.out;

import edu.escuelaing.dosw.brasaviva.dto.response.RegistroVehiculoResponseDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.RegistroVehiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegistroVehiculoMapperOut {

    @Mapping(target = "activo", expression = "java(registro.estaActivo())")
    RegistroVehiculoResponseDTO toDTO(RegistroVehiculo registro);
}