package edu.escuelaing.dosw.brasaviva.mapper.out;

import edu.escuelaing.dosw.brasaviva.dto.response.ItemPedidoResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PedidoResponseDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PedidoMapperOut {

    @Mapping(target = "total", expression = "java(pedido.calcularTotal())")
    PedidoResponseDTO toDTO(Pedido pedido);

    @Mapping(target = "subtotal", expression = "java(item.subtotal())")
    ItemPedidoResponseDTO toDTO(ItemPedido item);
}