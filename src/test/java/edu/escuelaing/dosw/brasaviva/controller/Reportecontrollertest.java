package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.response.IngresosResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoPopularDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ResumenDiaDTO;
import edu.escuelaing.dosw.brasaviva.exception.RangoFechasInvalidoException;
import edu.escuelaing.dosw.brasaviva.service.IReporteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReporteController.class)
class ReporteControllerTest {

    private static final String BASE = "/api/v1/reportes";

    @Autowired private MockMvc mockMvc;
    @MockBean private IReporteService reporteService;

    @Test
    @DisplayName("GET /reportes/resumen - 200")
    void resumen_debeRetornar200() throws Exception {
        ResumenDiaDTO resumen = new ResumenDiaDTO(5, 250000.0, Map.of("Picanha", 3L), Map.of("LISTO", 2L), 1);
        when(reporteService.resumenDelDia()).thenReturn(resumen);

        mockMvc.perform(get(BASE + "/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPedidos").value(5))
                .andExpect(jsonPath("$.ingresoTotal").value(250000.0))
                .andExpect(jsonPath("$.mesasConCuentaAbierta").value(1));
    }

    @Test
    @DisplayName("GET /reportes/platos-populares - usa top=5 por defecto")
    void platosPopulares_sinTop_debeUsarDefault5() throws Exception {
        when(reporteService.platosPopulares(5)).thenReturn(List.of(new PlatoPopularDTO("Picanha", 10)));

        mockMvc.perform(get(BASE + "/platos-populares"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombrePlato").value("Picanha"))
                .andExpect(jsonPath("$[0].cantidadVendida").value(10));

        verify(reporteService).platosPopulares(5);
    }

    @Test
    @DisplayName("GET /reportes/platos-populares?top=10 - pasa el top al servicio")
    void platosPopulares_conTop_debePasarTop() throws Exception {
        when(reporteService.platosPopulares(10)).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/platos-populares").param("top", "10"))
                .andExpect(status().isOk());

        verify(reporteService).platosPopulares(10);
    }

    @Test
    @DisplayName("GET /reportes/platos-populares?top=0 - 400 por debajo del minimo")
    void platosPopulares_topCero_debeRetornar400() throws Exception {
        mockMvc.perform(get(BASE + "/platos-populares").param("top", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parametros invalidos"))
                .andExpect(jsonPath("$.message").value(containsString("El top minimo es 1")));

        verifyNoInteractions(reporteService);
    }

    @Test
    @DisplayName("GET /reportes/platos-populares?top=51 - 400 por encima del maximo")
    void platosPopulares_topExcesivo_debeRetornar400() throws Exception {
        mockMvc.perform(get(BASE + "/platos-populares").param("top", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("El top maximo es 50")));
    }

    @Test
    @DisplayName("GET /reportes/platos-populares?top=abc - 400 por tipo incorrecto")
    void platosPopulares_topNoNumerico_debeRetornar400() throws Exception {
        mockMvc.perform(get(BASE + "/platos-populares").param("top", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parametro invalido"))
                .andExpect(jsonPath("$.message").value(containsString("top")));
    }

    @Test
    @DisplayName("GET /reportes/ingresos - 200 con rango valido")
    void ingresos_rangoValido_debeRetornar200() throws Exception {
        LocalDate desde = LocalDate.of(2026, 9, 1);
        LocalDate hasta = LocalDate.of(2026, 9, 28);
        when(reporteService.ingresos(desde, hasta))
                .thenReturn(new IngresosResponseDTO(desde, hasta, Map.of("CORTE", 500000.0), 500000.0));

        mockMvc.perform(get(BASE + "/ingresos").param("desde", "2026-09-01").param("hasta", "2026-09-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(500000.0))
                .andExpect(jsonPath("$.ingresosPorCategoria.CORTE").value(500000.0));
    }

    @Test
    @DisplayName("GET /reportes/ingresos - 400 cuando falta un parametro obligatorio")
    void ingresos_sinHasta_debeRetornar400() throws Exception {
        mockMvc.perform(get(BASE + "/ingresos").param("desde", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parametro faltante"))
                .andExpect(jsonPath("$.message").value(containsString("hasta")));

        verifyNoInteractions(reporteService);
    }

    @Test
    @DisplayName("GET /reportes/ingresos - 400 cuando la fecha tiene formato invalido")
    void ingresos_fechaMalFormada_debeRetornar400() throws Exception {
        mockMvc.perform(get(BASE + "/ingresos").param("desde", "01/09/2026").param("hasta", "2026-09-28"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parametro invalido"));
    }

    @Test
    @DisplayName("GET /reportes/ingresos - 400 cuando el rango es invalido (desde > hasta)")
    void ingresos_rangoInvalido_debeRetornar400() throws Exception {
        LocalDate desde = LocalDate.of(2026, 9, 28);
        LocalDate hasta = LocalDate.of(2026, 9, 1);
        when(reporteService.ingresos(desde, hasta)).thenThrow(new RangoFechasInvalidoException("desde debe ser <= hasta"));

        mockMvc.perform(get(BASE + "/ingresos").param("desde", "2026-09-28").param("hasta", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solicitud invalida"))
                .andExpect(jsonPath("$.message").value("desde debe ser <= hasta"));
    }
}