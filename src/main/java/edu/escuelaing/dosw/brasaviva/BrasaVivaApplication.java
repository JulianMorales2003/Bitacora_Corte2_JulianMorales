package edu.escuelaing.dosw.brasaviva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BrasaVivaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrasaVivaApplication.class, args);
    }
}