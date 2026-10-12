package edu.escuelaing.dosw.brasaviva.config;

import edu.escuelaing.dosw.brasaviva.dto.request.UsuarioRequestDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Rol;
import edu.escuelaing.dosw.brasaviva.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Crea un usuario de ejemplo por cada rol la primera vez que arranca la API con la BD vacia.
 * Es solo para desarrollo: en produccion se desactiva con {@code SEED_USUARIOS=false}.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "brasaviva.seguridad", name = "seed-usuarios", havingValue = "true", matchIfMissing = true)
public class UsuarioInitializer implements CommandLineRunner {

    private final IUsuarioService usuarioService;

    @Value("${brasaviva.seguridad.seed-password}")
    private String seedPassword;

    @Override
    public void run(String... args) {
        if (usuarioService.contar() > 0) {
            return;
        }
        crear("Administrador", "admin@brasaviva.com", Rol.ADMIN);
        crear("Mesero", "mesero@brasaviva.com", Rol.MESERO);
        crear("Parrillero", "parrillero@brasaviva.com", Rol.PARRILLERO);
        crear("Cliente", "cliente@brasaviva.com", Rol.CLIENTE);
        log.info("Usuarios de ejemplo cargados: 4 (uno por rol)");
    }

    private void crear(String nombre, String email, Rol rol) {
        usuarioService.crear(new UsuarioRequestDTO(nombre, email, seedPassword, rol));
    }
}
