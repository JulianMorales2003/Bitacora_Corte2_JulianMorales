package edu.escuelaing.dosw.brasaviva.persistence.mapper;

import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.persistence.entity.PedidoEntity;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.persistence.entity.ItemPedidoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** Traduce entre el modelo de dominio y la entidad de persistencia de Pedido. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PedidoEntityMapper {

    Pedido toDomain(PedidoEntity entity);

    PedidoEntity toEntity(Pedido domain);

    ItemPedido toDomain(ItemPedidoEntity entity);

    ItemPedidoEntity toEntity(ItemPedido domain);
}
