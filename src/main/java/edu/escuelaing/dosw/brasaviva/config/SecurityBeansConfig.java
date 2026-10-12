package edu.escuelaing.dosw.brasaviva.config;

import edu.escuelaing.dosw.brasaviva.security.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Beans base de seguridad. Viven aparte de {@link SecurityConfig} para evitar dependencias
 * circulares (los servicios necesitan el PasswordEncoder y SecurityConfig necesita los servicios).
 */
@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // OBLIGATORIO: nunca guardar passwords en texto plano
    }

    @Bean
    public AuthenticationManager authenticationManager(UsuarioDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
