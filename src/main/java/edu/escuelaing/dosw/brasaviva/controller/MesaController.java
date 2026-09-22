package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.request.MesaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.MesaResponseDTO;
import edu.escuelaing.dosw.brasaviva.service.IMesaService;
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
@RequestMapping("/api/v1/mesas")
@Tag(name = "Mesas", description = "El salon: estado, capacidad y disponibilidad")
@Slf4j
@RequiredArgsConstructor
public class MesaController {

    private final IMesaService mesaService;

    @Operation(summary = "Ver todas las mesas", description = "Filtro opcional por estado: DISPONIBLE, OCUPADA, RESERVADA")
    @ApiResponse(responseCode = "200", description = "Mesas obtenidas")
    @GetMapping
    public ResponseEntity<List<MesaResponseDTO>> obtenerTodas(@RequestParam(required = false) String estado) {
        log.info("GET /api/v1/mesas estado={}", estado);
        return ResponseEntity.ok(mesaService.obtenerTodas(estado));
    }

    @Operation(summary = "Ver una mesa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mesa encontrada"),
            @ApiResponse(responseCode = "404", description = "La mesa no existe")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MesaResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/v1/mesas/{}", id);
        return ResponseEntity.ok(mesaService.obtenerPorId(id));
    }

    @Operation(summary = "Crear una mesa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mesa creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe una mesa con ese numero")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MesaResponseDTO crear(@RequestBody @Valid MesaRequestDTO dto) {
        log.info("POST /api/v1/mesas - numero {}", dto.numero());
        return mesaService.crear(dto);
    }
}