package edu.escuelaing.dosw.brasaviva.service.impl;

import edu.escuelaing.dosw.brasaviva.dto.request.LoginRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.TokenResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.DemasiadosIntentosException;
import edu.escuelaing.dosw.brasaviva.security.JwtUtil;
import edu.escuelaing.dosw.brasaviva.security.LoginAttemptService;
import edu.escuelaing.dosw.brasaviva.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private static final String ROL_PREFIJO = "ROLE_";

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;

    @Override
    public TokenResponseDTO login(LoginRequestDTO dto) {
        String email = dto.email().trim().toLowerCase(Locale.ROOT);

        if (loginAttemptService.estaBloqueado(email)) {
            log.warn("Login bloqueado por exceso de intentos fallidos: {}", limpiar(email));
            throw new DemasiadosIntentosException("Demasiados intentos fallidos. Intenta de nuevo mas tarde");
        }

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, dto.password()));
        } catch (BadCredentialsException e) {
            loginAttemptService.registrarFallo(email);
            log.warn("Login fallido para {}", limpiar(email)); // OWASP A09: se registran los intentos fallidos
            throw e;
        }

        loginAttemptService.registrarExito(email);
        String rol = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("")
                .replaceFirst("^" + ROL_PREFIJO, "");
        log.info("Login exitoso: {} ({})", limpiar(email), rol);
        return new TokenResponseDTO(jwtUtil.generateToken(auth.getName(), rol), "Bearer",
                jwtUtil.getExpirationMs(), auth.getName(), rol);
    }

    /** Evita inyeccion de saltos de linea en los logs (log forging). */
    private String limpiar(String valor) {
        return valor.replaceAll("[\\r\\n\\t]", "_");
    }
}
