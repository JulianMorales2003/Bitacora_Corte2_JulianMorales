package edu.escuelaing.dosw.brasaviva.mapper.out;

import edu.escuelaing.dosw.brasaviva.dto.response.MesaResponseDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MesaMapperOut {

    MesaResponseDTO toDTO(Mesa mesa);
}