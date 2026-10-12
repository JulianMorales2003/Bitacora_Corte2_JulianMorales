package edu.escuelaing.dosw.brasaviva.mapper.out;

import edu.escuelaing.dosw.brasaviva.dto.response.UsuarioResponseDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioMapperOut {

    UsuarioResponseDTO toDTO(Usuario usuario);
}
