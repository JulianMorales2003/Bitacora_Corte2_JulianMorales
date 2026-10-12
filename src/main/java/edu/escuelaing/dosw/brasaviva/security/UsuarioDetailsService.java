package edu.escuelaing.dosw.brasaviva.security;

import edu.escuelaing.dosw.brasaviva.model.domain.Usuario;
import edu.escuelaing.dosw.brasaviva.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/** Le dice a Spring Security como cargar un usuario (por email) desde la base de datos. */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String correo = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByEmail(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword()) // ya viene hasheado desde la BD
                .roles(usuario.getRol().name())  // "ADMIN" -> authority ROLE_ADMIN
                .build();
    }
}