package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.request.UsuarioRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.UsuarioResponseDTO;
import edu.escuelaing.dosw.brasaviva.service.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Administracion de usuarios y roles (solo ADMIN)")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;

    @Operation(summary = "Ver todos los usuarios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios obtenidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Solo el administrador")
    })
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodos() {
        log.info("GET /api/v1/usuarios");
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @Operation(summary = "Crear un usuario con un rol", description = "Permite crear MESERO, PARRILLERO, ADMIN o CLIENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "403", description = "Solo el administrador"),
            @ApiResponse(responseCode = "409", description = "El email ya esta registrado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO crear(@RequestBody @Valid UsuarioRequestDTO dto) {
        log.info("POST /api/v1/usuarios - rol {}", dto.rol());
        return usuarioService.crear(dto);
    }
}
