package edu.escuelaing.dosw.brasaviva.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI brasaVivaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de La Brasa Viva")
                        .description("API REST del restaurante parrilla: carta, mesas, pedidos, cocina, cuentas, reservas, parqueadero y reportes")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Julian Felipe Morales Zambrano")
                                .email("julian.morales-z@mail.escuelaing.edu.co")));
    }
}