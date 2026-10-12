package edu.escuelaing.dosw.brasaviva.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI brasaVivaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de La Brasa Viva")
                        .description("API REST del restaurante parrilla: carta, mesas, pedidos, cocina, cuentas, reservas, parqueadero y reportes")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Julian Felipe Morales Zambrano")
                                .email("julian.morales-z@mail.escuelaing.edu.co")))
                // Boton "Authorize" de Swagger UI: se pega el JWT devuelto por /api/v1/auth/login
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}