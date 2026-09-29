package edu.escuelaing.dosw.brasaviva.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dosw.brasaviva.dto.request.ReservaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ReservaResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.*;
import edu.escuelaing.dosw.brasaviva.service.IReservaService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    private static final String BASE = "/api/v1/reservas";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private IReservaService reservaService;

    private ReservaRequestDTO requestValido() {
        return new ReservaRequestDTO(3L, "Carlos Perez", LocalDateTime.now().plusDays(2), 4);
    }

    private ReservaResponseDTO respuesta() {
        return new ReservaResponseDTO(1L, 3L, "Carlos Perez", LocalDateTime.now().plusDays(2), 4, "ACTIVA");
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    @DisplayName("POST /reservas - 201 con body valido")
    void crear_valido_debeRetornar201() throws Exception {
        when(reservaService.crear(any(ReservaRequestDTO.class))).thenReturn(respuesta());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente").value("Carlos Perez"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    @DisplayName("POST /reservas - 400 cuando la fecha esta en el pasado")
    void crear_fechaPasada_debeRetornar400() throws Exception {
        ReservaRequestDTO dto = new ReservaRequestDTO(3L, "Carlos Perez", LocalDateTime.now().minusDays(1), 4);

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("La reserva debe ser en una fecha futura"))));

        verifyNoInteractions(reservaService);
    }

    @Test
    @DisplayName("POST /reservas - 400 cuando faltan todos los campos")
    void crear_sinCampos_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("idMesa"))))
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("cliente"))))
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("fechaHora"))))
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("comensales"))));
    }

    @Test
    @DisplayName("POST /reservas - 400 cuando los comensales superan 20")
    void crear_demasiadosComensales_debeRetornar400() throws Exception {
        ReservaRequestDTO dto = new ReservaRequestDTO(3L, "Carlos Perez", LocalDateTime.now().plusDays(1), 21);

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Maximo 20 comensales por reserva"))));
    }

    @Test
    @DisplayName("POST /reservas - 400 cuando el nombre tiene un solo caracter")
    void crear_nombreCorto_debeRetornar400() throws Exception {
        ReservaRequestDTO dto = new ReservaRequestDTO(3L, "C", LocalDateTime.now().plusDays(1), 2);

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /reservas - 404 cuando la mesa no existe")
    void crear_mesaInexistente_debeRetornar404() throws Exception {
        when(reservaService.crear(any(ReservaRequestDTO.class))).thenThrow(new MesaNoEncontradaException("Mesa 3 no existe"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(requestValido())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /reservas - 409 cuando la mesa ya esta reservada en ese horario")
    void crear_conflictoHorario_debeRetornar409() throws Exception {
        when(reservaService.crear(any(ReservaRequestDTO.class))).thenThrow(new ReservaConflictoException("Mesa ya reservada"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(requestValido())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Mesa ya reservada"));
    }

    @Test
    @DisplayName("POST /reservas - 422 cuando los comensales superan la capacidad de la mesa")
    void crear_capacidadExcedida_debeRetornar422() throws Exception {
        when(reservaService.crear(any(ReservaRequestDTO.class))).thenThrow(new CapacidadMesaExcedidaException("Mesa de 2 personas"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(requestValido())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /reservas - 200 con filtro por cliente")
    void obtenerTodas_conCliente_debeRetornar200() throws Exception {
        when(reservaService.obtenerTodas("Carlos")).thenReturn(List.of(respuesta()));

        mockMvc.perform(get(BASE).param("cliente", "Carlos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(reservaService).obtenerTodas("Carlos");
    }

    @Test
    @DisplayName("GET /reservas/{id} - 200 cuando existe")
    void obtenerPorId_existente_debeRetornar200() throws Exception {
        when(reservaService.obtenerPorId(1L)).thenReturn(respuesta());

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comensales").value(4));
    }

    @Test
    @DisplayName("GET /reservas/{id} - 404 cuando no existe")
    void obtenerPorId_noExiste_debeRetornar404() throws Exception {
        when(reservaService.obtenerPorId(9L)).thenThrow(new ReservaNoEncontradaException("Reserva 9 no existe"));

        mockMvc.perform(get(BASE + "/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /reservas/{id} - 200 al reprogramar")
    void actualizar_valido_debeRetornar200() throws Exception {
        when(reservaService.actualizar(eq(1L), any(ReservaRequestDTO.class))).thenReturn(respuesta());

        mockMvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(json(requestValido())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /reservas/{id} - 400 con body invalido")
    void actualizar_bodyInvalido_debeRetornar400() throws Exception {
        mockMvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservaService);
    }

    @Test
    @DisplayName("PUT /reservas/{id} - 409 conflicto de horario")
    void actualizar_conflicto_debeRetornar409() throws Exception {
        when(reservaService.actualizar(eq(1L), any(ReservaRequestDTO.class))).thenThrow(new ReservaConflictoException("Horario ocupado"));

        mockMvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(json(requestValido())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /reservas/{id} - 422 cuando la reserva no esta activa")
    void actualizar_noActiva_debeRetornar422() throws Exception {
        when(reservaService.actualizar(eq(1L), any(ReservaRequestDTO.class))).thenThrow(new EstadoInvalidoException("Reserva no activa"));

        mockMvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(json(requestValido())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("DELETE /reservas/{id} - 204 al cancelar")
    void cancelar_valido_debeRetornar204() throws Exception {
        doNothing().when(reservaService).cancelar(1L);

        mockMvc.perform(delete(BASE + "/1")).andExpect(status().isNoContent());

        verify(reservaService).cancelar(1L);
    }

    @Test
    @DisplayName("DELETE /reservas/{id} - 404 cuando no existe")
    void cancelar_noExiste_debeRetornar404() throws Exception {
        doThrow(new ReservaNoEncontradaException("Reserva 9 no existe")).when(reservaService).cancelar(9L);

        mockMvc.perform(delete(BASE + "/9")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /reservas/{id} - 422 cuando ya estaba cancelada o completada")
    void cancelar_yaCancelada_debeRetornar422() throws Exception {
        doThrow(new EstadoInvalidoException("Ya cancelada")).when(reservaService).cancelar(1L);

        mockMvc.perform(delete(BASE + "/1")).andExpect(status().isUnprocessableEntity());
    }
}