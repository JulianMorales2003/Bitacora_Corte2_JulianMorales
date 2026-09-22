package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.request.DisponibilidadRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PlatoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoResponseDTO;
import edu.escuelaing.dosw.brasaviva.service.IPlatoService;
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
@RequestMapping("/api/v1/platos")
@Tag(name = "Platos", description = "Administracion de la carta (gerente)")
@Slf4j
@RequiredArgsConstructor
public class PlatoController {

    private final IPlatoService platoService;

    @Operation(summary = "Obtener todos los platos", description = "Incluye los agotados")
    @ApiResponse(responseCode = "200", description = "Carta obtenida correctamente")
    @GetMapping
    public ResponseEntity<List<PlatoResponseDTO>> obtenerTodos() {
        log.info("GET /api/v1/platos");
        return ResponseEntity.ok(platoService.obtenerTodos());
    }

    @Operation(summary = "Obtener un plato por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plato encontrado"),
            @ApiResponse(responseCode = "404", description = "El plato no existe")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PlatoResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/v1/platos/{}", id);
        return ResponseEntity.ok(platoService.obtenerPorId(id));
    }

    @Operation(summary = "Crear un plato", description = "Agrega un nuevo plato a la carta, disponible por defecto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plato creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un plato con ese nombre")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlatoResponseDTO crear(@RequestBody @Valid PlatoRequestDTO dto) {
        log.info("POST /api/v1/platos - {}", dto.nombre());
        return platoService.crear(dto);
    }

    @Operation(summary = "Actualizar un plato", description = "Reemplaza nombre, precio, categoria, descripcion y tiempo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plato actualizado"),
            @ApiResponse(responseCode = "400", description = "Body incompleto o invalido"),
            @ApiResponse(responseCode = "404", description = "El plato no existe"),
            @ApiResponse(responseCode = "409", description = "Otro plato ya usa ese nombre")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PlatoResponseDTO> actualizar(@PathVariable Long id,
                                                       @RequestBody @Valid PlatoRequestDTO dto) {
        log.info("PUT /api/v1/platos/{}", id);
        return ResponseEntity.ok(platoService.actualizar(id, dto));
    }

    @Operation(summary = "Marcar disponible o agotado", description = "RN-02: un plato agotado no se puede pedir")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada"),
            @ApiResponse(responseCode = "400", description = "Falta el campo disponible"),
            @ApiResponse(responseCode = "404", description = "El plato no existe")
    })
    @PatchMapping("/{id}/disponible")
    public ResponseEntity<PlatoResponseDTO> cambiarDisponibilidad(@PathVariable Long id,
                                                                  @RequestBody @Valid DisponibilidadRequestDTO dto) {
        log.info("PATCH /api/v1/platos/{}/disponible - {}", id, dto.disponible());
        return ResponseEntity.ok(platoService.cambiarDisponibilidad(id, dto));
    }

    @Operation(summary = "Eliminar un plato de la carta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Plato eliminado"),
            @ApiResponse(responseCode = "404", description = "El plato no existe")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/platos/{}", id);
        platoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}