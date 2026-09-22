package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.request.CambioEstadoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.ItemPedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PedidoResponseDTO;
import edu.escuelaing.dosw.brasaviva.service.IPedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos y Cocina", description = "Flujo del pedido: RECIBIDO -> EN_PREPARACION -> LISTO -> ENTREGADO")
@Slf4j
@RequiredArgsConstructor
public class PedidoController {

    private final IPedidoService pedidoService;

    @Operation(summary = "Confirmar un pedido", description = "Congela precios (RN-04). Los cortes requieren termino (RN-P01)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido confirmado en RECIBIDO"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "404", description = "La mesa o un plato no existen"),
            @ApiResponse(responseCode = "422", description = "Plato agotado, mesa sin cuenta, corte sin termino o corte lento cerca del cierre")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO crear(@RequestBody @Valid PedidoRequestDTO dto) {
        log.info("POST /api/v1/pedidos - mesa {}", dto.idMesa());
        return pedidoService.crear(dto);
    }

    @Operation(summary = "Ver todos los pedidos", description = "Filtro opcional por estado")
    @ApiResponse(responseCode = "200", description = "Pedidos obtenidos")
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> obtenerTodos(@RequestParam(required = false) String estado) {
        log.info("GET /api/v1/pedidos estado={}", estado);
        return ResponseEntity.ok(pedidoService.obtenerTodos(estado));
    }

    @Operation(summary = "Tablero de cocina", description = "Pedidos RECIBIDO y EN_PREPARACION en orden de llegada")
    @ApiResponse(responseCode = "200", description = "Tablero obtenido")
    @GetMapping("/cocina")
    public ResponseEntity<List<PedidoResponseDTO>> tableroCocina() {
        log.info("GET /api/v1/pedidos/cocina");
        return ResponseEntity.ok(pedidoService.obtenerTableroCocina());
    }

    @Operation(summary = "Ver pedidos activos de una mesa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedidos activos de la mesa"),
            @ApiResponse(responseCode = "404", description = "La mesa no existe")
    })
    @GetMapping("/mesa/{idMesa}")
    public ResponseEntity<List<PedidoResponseDTO>> activosPorMesa(@PathVariable Long idMesa) {
        log.info("GET /api/v1/pedidos/mesa/{}", idMesa);
        return ResponseEntity.ok(pedidoService.obtenerActivosPorMesa(idMesa));
    }

    @Operation(summary = "Ver un pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/v1/pedidos/{}", id);
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @Operation(summary = "Agregar un plato a un pedido", description = "RN-01: solo si el pedido sigue en RECIBIDO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item agregado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "404", description = "El pedido o el plato no existen"),
            @ApiResponse(responseCode = "422", description = "Pedido ya en cocina, plato agotado o corte sin termino")
    })
    @PostMapping("/{id}/items")
    public ResponseEntity<PedidoResponseDTO> agregarItem(@PathVariable Long id,
                                                         @RequestBody @Valid ItemPedidoRequestDTO dto) {
        log.info("POST /api/v1/pedidos/{}/items - plato {}", id, dto.idPlato());
        return ResponseEntity.ok(pedidoService.agregarItem(id, dto));
    }

    @Operation(summary = "Cambiar el estado de un pedido", description = "RN-P02: maximo de cortes simultaneos en la parrilla")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado con formato invalido"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe"),
            @ApiResponse(responseCode = "422", description = "Transicion invalida o parrilla llena")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(@PathVariable Long id,
                                                           @RequestBody @Valid CambioEstadoRequestDTO dto) {
        log.info("PATCH /api/v1/pedidos/{}/estado - {}", id, dto.estado());
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, dto));
    }

    @Operation(summary = "Cancelar un pedido", description = "Solo si sigue en RECIBIDO. Queda en CANCELADO para el historial")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido cancelado"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe"),
            @ApiResponse(responseCode = "422", description = "El pedido ya esta en cocina")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        log.info("DELETE /api/v1/pedidos/{}", id);
        pedidoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}