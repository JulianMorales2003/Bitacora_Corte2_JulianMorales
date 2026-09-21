package edu.escuelaing.dosw.brasaviva.mapper.out;

import edu.escuelaing.dosw.brasaviva.dto.response.ReservaResponseDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservaMapperOut {

    ReservaResponseDTO toDTO(Reserva reserva);
}