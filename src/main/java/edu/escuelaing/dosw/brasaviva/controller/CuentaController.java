package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.request.AbrirCuentaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PagoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.CuentaResponseDTO;
import edu.escuelaing.dosw.brasaviva.service.ICuentaService;
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

@RestController
@RequestMapping("/api/v1/cuentas")
@Tag(name = "Cuentas y Pagos", description = "Apertura de cuenta por mesa, total en vivo y cierre con pago")
@Slf4j
@RequiredArgsConstructor
public class CuentaController {

    private final ICuentaService cuentaService;

    @Operation(summary = "Abrir cuenta en una mesa", description = "RN-03: una sola cuenta abierta por mesa. Tambien sirve para clientes sin reserva (walk-in)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta abierta"),
            @ApiResponse(responseCode = "400", description = "Falta el id de la mesa"),
            @ApiResponse(responseCode = "404", description = "La mesa no existe"),
            @ApiResponse(responseCode = "422", description = "La mesa ya tiene una cuenta abierta")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaResponseDTO abrir(@RequestBody @Valid AbrirCuentaRequestDTO dto) {
        log.info("POST /api/v1/cuentas - mesa {}", dto.idMesa());
        return cuentaService.abrir(dto);
    }

    @Operation(summary = "Ver una cuenta", description = "Si esta abierta, el total se recalcula con los pedidos actuales")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "La cuenta no existe")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/v1/cuentas/{}", id);
        return ResponseEntity.ok(cuentaService.obtenerPorId(id));
    }

    @Operation(summary = "Ver la cuenta abierta de una mesa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta abierta encontrada"),
            @ApiResponse(responseCode = "404", description = "La mesa no existe o no tiene cuenta abierta")
    })
    @GetMapping("/mesa/{idMesa}")
    public ResponseEntity<CuentaResponseDTO> obtenerAbiertaPorMesa(@PathVariable Long idMesa) {
        log.info("GET /api/v1/cuentas/mesa/{}", idMesa);
        return ResponseEntity.ok(cuentaService.obtenerAbiertaPorMesa(idMesa));
    }

    @Operation(summary = "Registrar pago y cerrar la cuenta", description = "Todos los pedidos deben estar ENTREGADOS y el monto debe cubrir el total")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago registrado, cuenta cerrada y mesa liberada"),
            @ApiResponse(responseCode = "400", description = "Medio de pago o monto invalidos"),
            @ApiResponse(responseCode = "404", description = "La cuenta no existe"),
            @ApiResponse(responseCode = "422", description = "Cuenta ya cerrada, pedidos pendientes o pago insuficiente")
    })
    @PostMapping("/{id}/pago")
    public ResponseEntity<CuentaResponseDTO> registrarPago(@PathVariable Long id,
                                                           @RequestBody @Valid PagoRequestDTO dto) {
        log.info("POST /api/v1/cuentas/{}/pago - {}", id, dto.medioPago());
        return ResponseEntity.ok(cuentaService.registrarPago(id, dto));
    }
}