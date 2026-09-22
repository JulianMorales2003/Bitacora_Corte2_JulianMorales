package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.response.MenuItemResponseDTO;
import edu.escuelaing.dosw.brasaviva.service.IPlatoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
@Tag(name = "Menu", description = "Carta visible para el cliente: solo platos disponibles")
@Slf4j
@RequiredArgsConstructor
public class MenuController {

    private final IPlatoService platoService;

    @Operation(summary = "Ver el menu", description = "Platos disponibles, opcionalmente filtrados por categoria")
    @ApiResponse(responseCode = "200", description = "Menu obtenido (puede estar vacio)")
    @GetMapping
    public ResponseEntity<List<MenuItemResponseDTO>> obtenerMenu(
            @RequestParam(required = false) String categoria) {
        log.info("GET /api/v1/menu categoria={}", categoria);
        return ResponseEntity.ok(platoService.obtenerMenu(categoria));
    }

    @Operation(summary = "Ver el detalle de un plato del menu")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plato disponible"),
            @ApiResponse(responseCode = "404", description = "El plato no existe o esta agotado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> obtenerItem(@PathVariable Long id) {
        log.info("GET /api/v1/menu/{}", id);
        return ResponseEntity.ok(platoService.obtenerItemMenu(id));
    }
}