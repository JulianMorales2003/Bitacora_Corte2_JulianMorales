package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.LoginRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.TokenResponseDTO;

public interface IAuthService {

    /** Autentica con email y contrasena y devuelve un JWT. Lanza 401 si las credenciales son invalidas. */
    TokenResponseDTO login(LoginRequestDTO dto);
}
