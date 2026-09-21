package edu.escuelaing.dosw.brasaviva.mapper.out;

import edu.escuelaing.dosw.brasaviva.dto.response.CuentaResponseDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Cuenta;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CuentaMapperOut {

    CuentaResponseDTO toDTO(Cuenta cuenta);
}