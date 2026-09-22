package edu.escuelaing.dosw.brasaviva.config;

import edu.escuelaing.dosw.brasaviva.dto.request.MesaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PlatoRequestDTO;
import edu.escuelaing.dosw.brasaviva.service.IMesaService;
import edu.escuelaing.dosw.brasaviva.service.IPlatoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final IPlatoService platoService;
    private final IMesaService mesaService;

    @Override
    public void run(String... args) {
        platoService.crear(new PlatoRequestDTO("Picanha", 58000.0, "CORTE",
                "300 g de picanha a la brasa con sal gruesa", 20));
        platoService.crear(new PlatoRequestDTO("Tomahawk", 145000.0, "CORTE",
                "Corte de 1 kg para compartir", 35));
        platoService.crear(new PlatoRequestDTO("Churrasco", 52000.0, "CORTE",
                "400 g de churrasco con chimichurri de la casa", 18));
        platoService.crear(new PlatoRequestDTO("Chorizo santarrosano", 14000.0, "ENTRADA",
                "Con arepa y limon", 10));
        platoService.crear(new PlatoRequestDTO("Papa criolla", 9000.0, "ACOMPANAMIENTO",
                "Papa criolla frita con sal de ajo", 8));
        platoService.crear(new PlatoRequestDTO("Limonada de coco", 12000.0, "BEBIDA",
                null, 5));
        platoService.crear(new PlatoRequestDTO("Brownie con helado", 16000.0, "POSTRE",
                null, 7));

        for (int numero = 1; numero <= 6; numero++) {
            mesaService.crear(new MesaRequestDTO(numero, numero <= 4 ? 4 : 8));
        }
        log.info("Datos de ejemplo cargados: 7 platos y 6 mesas");
    }
}