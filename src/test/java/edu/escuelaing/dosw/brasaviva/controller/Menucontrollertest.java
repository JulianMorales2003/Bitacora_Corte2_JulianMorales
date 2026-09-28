package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.response.MenuItemResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.PlatoNoEncontradoException;
import edu.escuelaing.dosw.brasaviva.service.IPlatoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private IPlatoService platoService;

    private MenuItemResponseDTO item() {
        return new MenuItemResponseDTO(1L, "Picanha", 55000.0, "CORTE", "Corte premium");
    }

    @Test
    @DisplayName("GET /menu - 200 sin filtro llama al servicio con categoria null")
    void obtenerMenu_sinFiltro_debeRetornar200() throws Exception {
        when(platoService.obtenerMenu(null)).thenReturn(List.of(item()));

        mockMvc.perform(get("/api/v1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Picanha"));

        verify(platoService).obtenerMenu(null);
    }

    @Test
    @DisplayName("GET /menu?categoria=CORTE - pasa la categoria al servicio")
    void obtenerMenu_conCategoria_debePasarFiltro() throws Exception {
        when(platoService.obtenerMenu("CORTE")).thenReturn(List.of(item()));

        mockMvc.perform(get("/api/v1/menu").param("categoria", "CORTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(platoService).obtenerMenu("CORTE");
    }

    @Test
    @DisplayName("GET /menu - 200 con lista vacia")
    void obtenerMenu_vacio_debeRetornarListaVacia() throws Exception {
        when(platoService.obtenerMenu(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /menu/{id} - 200 cuando esta disponible")
    void obtenerItem_disponible_debeRetornar200() throws Exception {
        when(platoService.obtenerItemMenu(1L)).thenReturn(item());

        mockMvc.perform(get("/api/v1/menu/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /menu/{id} - 404 cuando no existe o esta agotado")
    void obtenerItem_noDisponible_debeRetornar404() throws Exception {
        when(platoService.obtenerItemMenu(5L)).thenThrow(new PlatoNoEncontradoException("Plato 5 no disponible"));

        mockMvc.perform(get("/api/v1/menu/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Plato 5 no disponible"));
    }
}