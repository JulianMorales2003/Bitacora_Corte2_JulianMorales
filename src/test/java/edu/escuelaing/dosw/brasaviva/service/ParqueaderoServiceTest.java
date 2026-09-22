package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.config.BrasaVivaProperties;
import edu.escuelaing.dosw.brasaviva.dto.request.EntradaVehiculoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.DisponibilidadParqueaderoDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.RegistroVehiculoResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.ParqueaderoLlenoException;
import edu.escuelaing.dosw.brasaviva.exception.PlacaYaRegistradaException;
import edu.escuelaing.dosw.brasaviva.exception.VehiculoNoEncontradoException;
import edu.escuelaing.dosw.brasaviva.mapper.out.RegistroVehiculoMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.RegistroVehiculo;
import edu.escuelaing.dosw.brasaviva.service.impl.ParqueaderoServiceImpl;
import edu.escuelaing.dosw.brasaviva.support.RelojDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParqueaderoServiceTest {

    @Mock
    private RegistroVehiculoMapperOut mapperOut;

    private RelojDePrueba reloj;
    private ParqueaderoServiceImpl parqueaderoService;

    @BeforeEach
    void setUp() {
        reloj = new RelojDePrueba(LocalDateTime.of(2026, 9, 21, 12, 0));
        BrasaVivaProperties propiedades = new BrasaVivaProperties(8, LocalTime.of(22, 0), 30, 25, 2, 3000.0);
        parqueaderoService = new ParqueaderoServiceImpl(mapperOut, propiedades, reloj);

        lenient().when(mapperOut.toDTO(any(RegistroVehiculo.class))).thenAnswer(inv -> {
            RegistroVehiculo r = inv.getArgument(0);
            return new RegistroVehiculoResponseDTO(r.getId(), r.getPlaca(), r.getEntrada(), r.getSalida(),
                    r.getCobro(), r.estaActivo());
        });
    }

    private EntradaVehiculoRequestDTO placa(String placa) {
        return new EntradaVehiculoRequestDTO(placa);
    }

    @Test
    @DisplayName("Registrar entrada - vehiculo activo con la placa en mayusculas")
    void registrarEntrada_valida_debeQuedarActivo() {
        RegistroVehiculoResponseDTO resultado = parqueaderoService.registrarEntrada(placa("abc123"));

        assertEquals("ABC123", resultado.placa());
        assertTrue(resultado.activo());
        assertEquals(LocalDateTime.of(2026, 9, 21, 12, 0), resultado.entrada());
    }

    @Test
    @DisplayName("Registrar una placa que ya esta dentro - PlacaYaRegistradaException")
    void registrarEntrada_placaDuplicada_debeLanzarExcepcion() {
        parqueaderoService.registrarEntrada(placa("ABC123"));

        assertThrows(PlacaYaRegistradaException.class, () -> parqueaderoService.registrarEntrada(placa("abc123")));
    }

    @Test
    @DisplayName("Parqueadero lleno - ParqueaderoLlenoException")
    void registrarEntrada_lleno_debeLanzarExcepcion() {
        parqueaderoService.registrarEntrada(placa("AAA111"));
        parqueaderoService.registrarEntrada(placa("BBB222"));

        assertThrows(ParqueaderoLlenoException.class, () -> parqueaderoService.registrarEntrada(placa("CCC333")));
    }

    @Test
    @DisplayName("Salida despues de 2h10m - cobra 3 horas (hora o fraccion)")
    void registrarSalida_cobraPorHoraOFraccion() {
        parqueaderoService.registrarEntrada(placa("ABC123"));
        reloj.adelantar(Duration.ofMinutes(130));

        RegistroVehiculoResponseDTO resultado = parqueaderoService.registrarSalida("abc123");

        assertEquals(9000.0, resultado.cobro());
        assertFalse(resultado.activo());
    }

    @Test
    @DisplayName("Salida a los 10 minutos - cobra minimo una hora")
    void registrarSalida_cobroMinimoUnaHora() {
        parqueaderoService.registrarEntrada(placa("ABC123"));
        reloj.adelantar(Duration.ofMinutes(10));

        assertEquals(3000.0, parqueaderoService.registrarSalida("ABC123").cobro());
    }

    @Test
    @DisplayName("Salida de una placa sin ingreso - VehiculoNoEncontradoException")
    void registrarSalida_placaInexistente_debeLanzarExcepcion() {
        assertThrows(VehiculoNoEncontradoException.class, () -> parqueaderoService.registrarSalida("XYZ999"));
    }

    @Test
    @DisplayName("Un vehiculo que ya salio puede volver a entrar y libera cupo")
    void salida_liberaCupoYPermiteReingreso() {
        parqueaderoService.registrarEntrada(placa("AAA111"));
        parqueaderoService.registrarEntrada(placa("BBB222"));
        parqueaderoService.registrarSalida("AAA111");

        assertDoesNotThrow(() -> parqueaderoService.registrarEntrada(placa("AAA111")));
        assertEquals(2, parqueaderoService.obtenerActivos().size());
    }

    @Test
    @DisplayName("Disponibilidad - capacidad, ocupados y disponibles")
    void obtenerDisponibilidad_debeContarCupos() {
        parqueaderoService.registrarEntrada(placa("AAA111"));

        DisponibilidadParqueaderoDTO resultado = parqueaderoService.obtenerDisponibilidad();

        assertEquals(2, resultado.capacidad());
        assertEquals(1, resultado.ocupados());
        assertEquals(1, resultado.disponibles());
    }

    @Test
    @DisplayName("Sin vehiculos - lista vacia")
    void obtenerActivos_vacio_debeRetornarListaVacia() {
        assertTrue(parqueaderoService.obtenerActivos().isEmpty());
    }
}