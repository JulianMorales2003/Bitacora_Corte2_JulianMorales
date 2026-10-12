package edu.escuelaing.dosw.brasaviva.service.impl;

import edu.escuelaing.dosw.brasaviva.dto.request.RegistroRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.UsuarioRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.UsuarioResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.UsuarioNoEncontradoException;
import edu.escuelaing.dosw.brasaviva.exception.UsuarioYaExisteException;
import edu.escuelaing.dosw.brasaviva.mapper.out.UsuarioMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.Rol;
import edu.escuelaing.dosw.brasaviva.model.domain.Usuario;
import edu.escuelaing.dosw.brasaviva.repository.UsuarioRepository;
import edu.escuelaing.dosw.brasaviva.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapperOut mapperOut;

    @Override
    public UsuarioResponseDTO registrarCliente(RegistroRequestDTO dto) {
        return guardar(dto.nombre(), dto.email(), dto.password(), Rol.CLIENTE);
    }

    @Override
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        return guardar(dto.nombre(), dto.email(), dto.password(), dto.rol());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream().map(mapperOut::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorEmail(String email) {
        return mapperOut.toDTO(usuarioRepository.findByEmail(normalizar(email))
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado")));
    }

    @Override
    public UsuarioResponseDTO obtenerOCrearCliente(String email, String nombre) {
        String correo = normalizar(email);
        return usuarioRepository.findByEmail(correo)
                .map(mapperOut::toDTO)
                .orElseGet(() -> guardar(nombre == null || nombre.isBlank() ? correo : nombre,
                        correo, UUID.randomUUID().toString(), Rol.CLIENTE));
    }

    @Override
    @Transactional(readOnly = true)
    public long contar() {
        return usuarioRepository.count();
    }

    private UsuarioResponseDTO guardar(String nombre, String email, String passwordPlano, Rol rol) {
        String correo = normalizar(email);
        if (usuarioRepository.existsByEmail(correo)) {
            throw new UsuarioYaExisteException("Ya existe un usuario con ese email");
        }
        Usuario usuario = Usuario.builder()
                .nombre(nombre)
                .email(correo)
                .password(passwordEncoder.encode(passwordPlano)) // siempre hasheado (BCrypt)
                .rol(rol)
                .build();
        usuario = usuarioRepository.save(usuario);
        log.info("Usuario creado: id={}, rol={}", usuario.getId(), usuario.getRol());
        return mapperOut.toDTO(usuario);
    }

    private String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
