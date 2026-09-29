package edu.escuelaing.dosw.brasaviva.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dosw.brasaviva.dto.request.MesaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.MesaResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.MesaNoEncontradaException;
import edu.escuelaing.dosw.brasaviva.exception.MesaYaExisteException;
import edu.escuelaing.dosw.brasaviva.service.IMesaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MesaController.class)
class MesaControllerTest {

    private static final String BASE = "/api/v1/mesas";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private IMesaService mesaService;

    private MesaResponseDTO mesa() {
        return new MesaResponseDTO(1L, 5, 4, "DISPONIBLE", false);
    }

    @Test
    @DisplayName("GET /mesas - 200 sin filtro")
    void obtenerTodas_sinFiltro_debeRetornar200() throws Exception {
        when(mesaService.obtenerTodas(null)).thenReturn(List.of(mesa()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value(5));
    }

    @Test
    @DisplayName("GET /mesas?estado=OCUPADA - pasa el filtro al servicio")
    void obtenerTodas_conEstado_debePasarFiltro() throws Exception {
        when(mesaService.obtenerTodas("OCUPADA")).thenReturn(List.of());

        mockMvc.perform(get(BASE).param("estado", "OCUPADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(mesaService).obtenerTodas("OCUPADA");
    }

    @Test
    @DisplayName("GET /mesas/{id} - 200 cuando existe")
    void obtenerPorId_existente_debeRetornar200() throws Exception {
        when(mesaService.obtenerPorId(1L)).thenReturn(mesa());

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"))
                .andExpect(jsonPath("$.cuentaAbierta").value(false));
    }

    @Test
    @DisplayName("GET /mesas/{id} - 404 cuando no existe")
    void obtenerPorId_noExiste_debeRetornar404() throws Exception {
        when(mesaService.obtenerPorId(9L)).thenThrow(new MesaNoEncontradaException("Mesa 9 no existe"));

        mockMvc.perform(get(BASE + "/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mesa 9 no existe"));
    }

    @Test
    @DisplayName("POST /mesas - 201 con body valido")
    void crear_valido_debeRetornar201() throws Exception {
        when(mesaService.crear(any(MesaRequestDTO.class))).thenReturn(mesa());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MesaRequestDTO(5, 4))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /mesas - 400 cuando numero y capacidad faltan")
    void crear_sinCampos_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("numero"))))
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("capacidad"))));

        verifyNoInteractions(mesaService);
    }

    @Test
    @DisplayName("POST /mesas - 400 cuando la capacidad supera 20")
    void crear_capacidadExcesiva_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MesaRequestDTO(5, 21))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("La capacidad maxima es 20 personas"))));
    }

    @Test
    @DisplayName("POST /mesas - 400 cuando el numero es cero")
    void crear_numeroNoPositivo_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MesaRequestDTO(0, 4))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("El numero de mesa debe ser positivo"))));
    }

    @Test
    @DisplayName("POST /mesas - 409 cuando el numero ya existe")
    void crear_numeroDuplicado_debeRetornar409() throws Exception {
        when(mesaService.crear(any(MesaRequestDTO.class))).thenThrow(new MesaYaExisteException("Ya existe la mesa 5"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MesaRequestDTO(5, 4))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}