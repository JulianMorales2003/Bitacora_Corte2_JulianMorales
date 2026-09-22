package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.CambioEstadoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.ItemPedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PedidoResponseDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;

import java.time.LocalDateTime;
import java.util.List;

public interface IPedidoService {

    PedidoResponseDTO crear(PedidoRequestDTO dto);

    PedidoResponseDTO agregarItem(Long idPedido, ItemPedidoRequestDTO dto);

    List<PedidoResponseDTO> obtenerTodos(String estado);

    PedidoResponseDTO obtenerPorId(Long id);

    List<PedidoResponseDTO> obtenerActivosPorMesa(Long idMesa);

    List<PedidoResponseDTO> obtenerTableroCocina();

    PedidoResponseDTO cambiarEstado(Long id, CambioEstadoRequestDTO dto);

    void cancelar(Long id);

    List<Pedido> obtenerEntidades();

    List<Pedido> obtenerEntidadesPorMesaDesde(Long idMesa, LocalDateTime desde);
}