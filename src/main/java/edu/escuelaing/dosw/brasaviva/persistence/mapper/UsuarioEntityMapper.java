package edu.escuelaing.dosw.brasaviva.persistence.mapper;

import edu.escuelaing.dosw.brasaviva.model.domain.Usuario;
import edu.escuelaing.dosw.brasaviva.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** Traduce entre el modelo de dominio y la entidad de persistencia de Usuario. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioEntityMapper {

    Usuario toDomain(UsuarioEntity entity);

    UsuarioEntity toEntity(Usuario domain);
}
