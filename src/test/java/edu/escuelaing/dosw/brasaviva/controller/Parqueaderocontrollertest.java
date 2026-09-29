package edu.escuelaing.dosw.brasaviva.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dosw.brasaviva.dto.request.EntradaVehiculoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.DisponibilidadParqueaderoDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.RegistroVehiculoResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.ParqueaderoLlenoException;
import edu.escuelaing.dosw.brasaviva.exception.PlacaYaRegistradaException;
import edu.escuelaing.dosw.brasaviva.exception.VehiculoNoEncontradoException;
import edu.escuelaing.dosw.brasaviva.service.IParqueaderoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParqueaderoController.class)
class ParqueaderoControllerTest {

    private static final String BASE = "/api/v1/parqueadero";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private IParqueaderoService parqueaderoService;

    private RegistroVehiculoResponseDTO activo() {
        return new RegistroVehiculoResponseDTO(1L, "ABC123", LocalDateTime.now(), null, null, true);
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    @DisplayName("POST /parqueadero/entrada - 201 con placa de carro")
    void registrarEntrada_carro_debeRetornar201() throws Exception {
        when(parqueaderoService.registrarEntrada(any(EntradaVehiculoRequestDTO.class))).thenReturn(activo());

        mockMvc.perform(post(BASE + "/entrada").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new EntradaVehiculoRequestDTO("ABC123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("ABC123"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("POST /parqueadero/entrada - 201 con placa de moto")
    void registrarEntrada_moto_debeRetornar201() throws Exception {
        when(parqueaderoService.registrarEntrada(any(EntradaVehiculoRequestDTO.class)))
                .thenReturn(new RegistroVehiculoResponseDTO(2L, "ABC12D", LocalDateTime.now(), null, null, true));

        mockMvc.perform(post(BASE + "/entrada").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new EntradaVehiculoRequestDTO("ABC12D"))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /parqueadero/entrada - 400 con placa de formato invalido")
    void registrarEntrada_placaInvalida_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE + "/entrada").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new EntradaVehiculoRequestDTO("12"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Placa invalida"))));

        verifyNoInteractions(parqueaderoService);
    }

    @Test
    @DisplayName("POST /parqueadero/entrada - 400 con placa en blanco")
    void registrarEntrada_placaVacia_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE + "/entrada").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("La placa es obligatoria"))));
    }

    @Test
    @DisplayName("POST /parqueadero/entrada - 409 cuando la placa ya esta dentro")
    void registrarEntrada_placaDuplicada_debeRetornar409() throws Exception {
        when(parqueaderoService.registrarEntrada(any(EntradaVehiculoRequestDTO.class)))
                .thenThrow(new PlacaYaRegistradaException("ABC123 ya esta dentro"));

        mockMvc.perform(post(BASE + "/entrada").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new EntradaVehiculoRequestDTO("ABC123"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /parqueadero/entrada - 422 cuando el parqueadero esta lleno")
    void registrarEntrada_lleno_debeRetornar422() throws Exception {
        when(parqueaderoService.registrarEntrada(any(EntradaVehiculoRequestDTO.class)))
                .thenThrow(new ParqueaderoLlenoException("Parqueadero lleno"));

        mockMvc.perform(post(BASE + "/entrada").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new EntradaVehiculoRequestDTO("ABC123"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Parqueadero lleno"));
    }

    @Test
    @DisplayName("POST /parqueadero/salida/{placa} - 200 con cobro")
    void registrarSalida_valida_debeRetornar200() throws Exception {
        RegistroVehiculoResponseDTO salida = new RegistroVehiculoResponseDTO(1L, "ABC123",
                LocalDateTime.now().minusHours(2), LocalDateTime.now(), 6000.0, false);
        when(parqueaderoService.registrarSalida("ABC123")).thenReturn(salida);

        mockMvc.perform(post(BASE + "/salida/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobro").value(6000.0))
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @DisplayName("POST /parqueadero/salida/{placa} - 404 cuando no hay ingreso activo")
    void registrarSalida_sinIngreso_debeRetornar404() throws Exception {
        when(parqueaderoService.registrarSalida("XYZ999")).thenThrow(new VehiculoNoEncontradoException("Sin ingreso activo"));

        mockMvc.perform(post(BASE + "/salida/XYZ999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /parqueadero/activos - 200")
    void obtenerActivos_debeRetornar200() throws Exception {
        when(parqueaderoService.obtenerActivos()).thenReturn(List.of(activo()));

        mockMvc.perform(get(BASE + "/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /parqueadero/disponibilidad - 200")
    void obtenerDisponibilidad_debeRetornar200() throws Exception {
        when(parqueaderoService.obtenerDisponibilidad()).thenReturn(new DisponibilidadParqueaderoDTO(20, 5, 15));

        mockMvc.perform(get(BASE + "/disponibilidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidad").value(20))
                .andExpect(jsonPath("$.ocupados").value(5))
                .andExpect(jsonPath("$.disponibles").value(15));
    }
}