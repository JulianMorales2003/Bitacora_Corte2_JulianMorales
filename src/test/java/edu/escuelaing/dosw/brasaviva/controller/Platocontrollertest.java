package edu.escuelaing.dosw.brasaviva.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dosw.brasaviva.dto.request.DisponibilidadRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PlatoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.PlatoNoEncontradoException;
import edu.escuelaing.dosw.brasaviva.exception.PlatoYaExisteException;
import edu.escuelaing.dosw.brasaviva.service.IPlatoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlatoController.class)
class PlatoControllerTest {

    private static final String BASE = "/api/v1/platos";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private IPlatoService platoService;

    private PlatoResponseDTO plato() {
        return new PlatoResponseDTO(1L, "Picanha", 55000.0, "CORTE", "Corte premium", true, 25);
    }

    private PlatoRequestDTO requestValido() {
        return new PlatoRequestDTO("Picanha", 55000.0, "CORTE", "Corte premium", 25);
    }

    @Test
    @DisplayName("GET /platos - 200 con la lista de platos")
    void obtenerTodos_debeRetornar200() throws Exception {
        when(platoService.obtenerTodos()).thenReturn(List.of(plato()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Picanha"));
    }

    @Test
    @DisplayName("GET /platos/{id} - 200 cuando existe")
    void obtenerPorId_existente_debeRetornar200() throws Exception {
        when(platoService.obtenerPorId(1L)).thenReturn(plato());

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoria").value("CORTE"));
    }

    @Test
    @DisplayName("GET /platos/{id} - 404 con ErrorResponseDTO cuando no existe")
    void obtenerPorId_noExiste_debeRetornar404() throws Exception {
        when(platoService.obtenerPorId(99L)).thenThrow(new PlatoNoEncontradoException("Plato 99 no existe"));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Plato 99 no existe"))
                .andExpect(jsonPath("$.path").value(BASE + "/99"));
    }

    @Test
    @DisplayName("GET /platos/{id} - 400 cuando el id no es numerico")
    void obtenerPorId_idNoNumerico_debeRetornar400() throws Exception {
        mockMvc.perform(get(BASE + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verifyNoInteractions(platoService);
    }

    @Test
    @DisplayName("POST /platos - 201 con body valido")
    void crear_valido_debeRetornar201() throws Exception {
        when(platoService.crear(any(PlatoRequestDTO.class))).thenReturn(plato());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Picanha"));

        verify(platoService, times(1)).crear(any(PlatoRequestDTO.class));
    }

    @Test
    @DisplayName("POST /platos - 400 cuando faltan campos obligatorios")
    void crear_bodyVacio_debeRetornar400ConDetalle() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("nombre"))))
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("precio"))));

        verifyNoInteractions(platoService);
    }

    @Test
    @DisplayName("POST /platos - 400 cuando la categoria no es valida")
    void crear_categoriaInvalida_debeRetornar400() throws Exception {
        PlatoRequestDTO dto = new PlatoRequestDTO("Picanha", 55000.0, "PIZZA", null, 25);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Categoria invalida"))));
    }

    @Test
    @DisplayName("POST /platos - 400 cuando el precio es cero o negativo")
    void crear_precioInvalido_debeRetornar400() throws Exception {
        PlatoRequestDTO dto = new PlatoRequestDTO("Picanha", 0.0, "CORTE", null, 25);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("El precio debe ser mayor a cero"))));
    }

    @Test
    @DisplayName("POST /platos - 400 cuando el JSON esta mal formado")
    void crear_jsonMalFormado_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nombre: sin comillas"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /platos - 409 cuando el nombre ya existe")
    void crear_nombreDuplicado_debeRetornar409() throws Exception {
        when(platoService.crear(any(PlatoRequestDTO.class)))
                .thenThrow(new PlatoYaExisteException("Ya existe un plato llamado Picanha"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("PUT /platos/{id} - 200 al actualizar")
    void actualizar_valido_debeRetornar200() throws Exception {
        when(platoService.actualizar(eq(1L), any(PlatoRequestDTO.class))).thenReturn(plato());

        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /platos/{id} - 404 cuando el plato no existe")
    void actualizar_noExiste_debeRetornar404() throws Exception {
        when(platoService.actualizar(eq(9L), any(PlatoRequestDTO.class)))
                .thenThrow(new PlatoNoEncontradoException("Plato 9 no existe"));

        mockMvc.perform(put(BASE + "/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /platos/{id} - 400 con body incompleto")
    void actualizar_bodyIncompleto_debeRetornar400() throws Exception {
        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"X\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(platoService);
    }

    @Test
    @DisplayName("PATCH /platos/{id}/disponible - 200 al marcar agotado")
    void cambiarDisponibilidad_valido_debeRetornar200() throws Exception {
        PlatoResponseDTO agotado = new PlatoResponseDTO(1L, "Picanha", 55000.0, "CORTE", null, false, 25);
        when(platoService.cambiarDisponibilidad(eq(1L), any(DisponibilidadRequestDTO.class))).thenReturn(agotado);

        mockMvc.perform(patch(BASE + "/1/disponible")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"disponible\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));
    }

    @Test
    @DisplayName("PATCH /platos/{id}/disponible - 400 cuando falta el campo disponible")
    void cambiarDisponibilidad_sinCampo_debeRetornar400() throws Exception {
        mockMvc.perform(patch(BASE + "/1/disponible")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Debe indicar si el plato esta disponible"))));
    }

    @Test
    @DisplayName("DELETE /platos/{id} - 204 al eliminar")
    void eliminar_existente_debeRetornar204() throws Exception {
        doNothing().when(platoService).eliminar(1L);

        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isNoContent());

        verify(platoService, times(1)).eliminar(1L);
    }

    @Test
    @DisplayName("DELETE /platos/{id} - 404 cuando no existe")
    void eliminar_noExiste_debeRetornar404() throws Exception {
        doThrow(new PlatoNoEncontradoException("Plato 9 no existe")).when(platoService).eliminar(9L);

        mockMvc.perform(delete(BASE + "/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /platos - 405 cuando el metodo no esta soportado")
    void metodoNoSoportado_debeRetornar405() throws Exception {
        mockMvc.perform(patch(BASE))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }
}