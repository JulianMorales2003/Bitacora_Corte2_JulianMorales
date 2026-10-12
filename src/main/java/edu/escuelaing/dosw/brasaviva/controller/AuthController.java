package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.request.LoginRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.RegistroRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.TokenResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.UsuarioResponseDTO;
import edu.escuelaing.dosw.brasaviva.service.IAuthService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Login con JWT, registro de clientes y consulta del usuario actual")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final IUsuarioService usuarioService;

    @Operation(summary = "Iniciar sesion", description = "Devuelve un JWT para usar en el boton Authorize de Swagger")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso, devuelve el token"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas"),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos fallidos")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        log.info("POST /api/v1/auth/login");
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Registrarse como cliente", description = "Siempre crea un usuario con rol CLIENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente registrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "409", description = "El email ya esta registrado")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO registrar(@RequestBody @Valid RegistroRequestDTO dto) {
        log.info("POST /api/v1/auth/register");
        return usuarioService.registrarCliente(dto);
    }

    @Operation(summary = "Ver el usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actual"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.obtenerPorEmail(authentication.getName()));
    }
}
