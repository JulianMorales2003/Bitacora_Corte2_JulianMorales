package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.request.EntradaVehiculoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.DisponibilidadParqueaderoDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.RegistroVehiculoResponseDTO;
import edu.escuelaing.dosw.brasaviva.service.IParqueaderoService;
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
@RequestMapping("/api/v1/parqueadero")
@Tag(name = "Parqueadero", description = "Entrada y salida de vehiculos, cobro por hora o fraccion")
@Slf4j
@RequiredArgsConstructor
public class ParqueaderoController {

    private final IParqueaderoService parqueaderoService;

    @Operation(summary = "Registrar entrada de un vehiculo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entrada registrada"),
            @ApiResponse(responseCode = "400", description = "Placa con formato invalido"),
            @ApiResponse(responseCode = "409", description = "La placa ya esta dentro"),
            @ApiResponse(responseCode = "422", description = "Parqueadero lleno")
    })
    @PostMapping("/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroVehiculoResponseDTO registrarEntrada(@RequestBody @Valid EntradaVehiculoRequestDTO dto) {
        log.info("POST /api/v1/parqueadero/entrada - {}", dto.placa());
        return parqueaderoService.registrarEntrada(dto);
    }

    @Operation(summary = "Registrar salida y calcular el cobro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Salida registrada con su cobro"),
            @ApiResponse(responseCode = "404", description = "La placa no tiene un ingreso activo")
    })
    @PostMapping("/salida/{placa}")
    public ResponseEntity<RegistroVehiculoResponseDTO> registrarSalida(@PathVariable String placa) {
        log.info("POST /api/v1/parqueadero/salida/{}", placa);
        return ResponseEntity.ok(parqueaderoService.registrarSalida(placa));
    }

    @Operation(summary = "Ver vehiculos activos")
    @ApiResponse(responseCode = "200", description = "Vehiculos dentro del parqueadero")
    @GetMapping("/activos")
    public ResponseEntity<List<RegistroVehiculoResponseDTO>> obtenerActivos() {
        log.info("GET /api/v1/parqueadero/activos");
        return ResponseEntity.ok(parqueaderoService.obtenerActivos());
    }

    @Operation(summary = "Ver cupos disponibles")
    @ApiResponse(responseCode = "200", description = "Capacidad, ocupados y disponibles")
    @GetMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadParqueaderoDTO> obtenerDisponibilidad() {
        log.info("GET /api/v1/parqueadero/disponibilidad");
        return ResponseEntity.ok(parqueaderoService.obtenerDisponibilidad());
    }
}