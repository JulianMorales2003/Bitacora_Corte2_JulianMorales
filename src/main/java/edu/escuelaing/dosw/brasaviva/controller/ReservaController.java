package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.request.ReservaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ReservaResponseDTO;
import edu.escuelaing.dosw.brasaviva.service.IReservaService;
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
@RequestMapping("/api/v1/reservas")
@Tag(name = "Reservas", description = "Reservas de mesa con bloqueo de 2 horas")
@Slf4j
@RequiredArgsConstructor
public class ReservaController {

    private final IReservaService reservaService;

    @Operation(summary = "Crear una reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o fecha en el pasado"),
            @ApiResponse(responseCode = "404", description = "La mesa no existe"),
            @ApiResponse(responseCode = "409", description = "Mesa ya reservada en ese horario"),
            @ApiResponse(responseCode = "422", description = "Los comensales superan la capacidad de la mesa")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponseDTO crear(@RequestBody @Valid ReservaRequestDTO dto) {
        log.info("POST /api/v1/reservas - mesa {} para {}", dto.idMesa(), dto.cliente());
        return reservaService.crear(dto);
    }

    @Operation(summary = "Ver reservas", description = "Filtro opcional por nombre de cliente")
    @ApiResponse(responseCode = "200", description = "Reservas obtenidas")
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> obtenerTodas(@RequestParam(required = false) String cliente) {
        log.info("GET /api/v1/reservas cliente={}", cliente);
        return ResponseEntity.ok(reservaService.obtenerTodas(cliente));
    }

    @Operation(summary = "Ver una reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "La reserva no existe")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/v1/reservas/{}", id);
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @Operation(summary = "Actualizar o reprogramar una reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "404", description = "La reserva o la mesa no existen"),
            @ApiResponse(responseCode = "409", description = "Conflicto de horario"),
            @ApiResponse(responseCode = "422", description = "Reserva no activa o capacidad excedida")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> actualizar(@PathVariable Long id,
                                                         @RequestBody @Valid ReservaRequestDTO dto) {
        log.info("PUT /api/v1/reservas/{}", id);
        return ResponseEntity.ok(reservaService.actualizar(id, dto));
    }

    @Operation(summary = "Cancelar una reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reserva cancelada"),
            @ApiResponse(responseCode = "404", description = "La reserva no existe"),
            @ApiResponse(responseCode = "422", description = "La reserva ya estaba cancelada o completada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        log.info("DELETE /api/v1/reservas/{}", id);
        reservaService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}