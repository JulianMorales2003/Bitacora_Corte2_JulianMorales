package edu.escuelaing.dosw.brasaviva.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dosw.brasaviva.dto.response.TokenResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.UsuarioResponseDTO;
import edu.escuelaing.dosw.brasaviva.service.IUsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Tras el login con Google: crea (o actualiza) el usuario local como CLIENTE y devuelve un JWT
 * propio del sistema. Asi se combina OAuth2 (identidad externa) con JWT (sesion stateless).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final IUsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String nombre = oauthUser.getAttribute("name");
        if (email == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "El proveedor no entrego un email");
            return;
        }
        UsuarioResponseDTO usuario = usuarioService.obtenerOCrearCliente(email, nombre);
        log.info("Login OAuth2 exitoso: id={}, rol={}", usuario.id(), usuario.rol());

        String rol = usuario.rol().name();
        TokenResponseDTO token = new TokenResponseDTO(jwtUtil.generateToken(usuario.email(), rol),
                "Bearer", jwtUtil.getExpirationMs(), usuario.email(), rol);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), token);
    }
}
