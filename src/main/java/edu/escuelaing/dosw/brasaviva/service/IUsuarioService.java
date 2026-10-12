package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.RegistroRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.UsuarioRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.UsuarioResponseDTO;

import java.util.List;

public interface IUsuarioService {

    /** Registro publico: crea un usuario con rol CLIENTE. */
    UsuarioResponseDTO registrarCliente(RegistroRequestDTO dto);

    /** Alta hecha por un administrador, con el rol elegido. */
    UsuarioResponseDTO crear(UsuarioRequestDTO dto);

    List<UsuarioResponseDTO> obtenerTodos();

    UsuarioResponseDTO obtenerPorEmail(String email);

    /** Usado por el login con OAuth2: devuelve el usuario o lo crea como CLIENTE si no existe. */
    UsuarioResponseDTO obtenerOCrearCliente(String email, String nombre);

    long contar();
}
