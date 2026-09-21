package edu.escuelaing.dosw.brasaviva.mapper.out;

import edu.escuelaing.dosw.brasaviva.dto.response.MenuItemResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoResponseDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlatoMapperOut {

    PlatoResponseDTO toDTO(Plato plato);

    MenuItemResponseDTO toMenuDTO(Plato plato);
}