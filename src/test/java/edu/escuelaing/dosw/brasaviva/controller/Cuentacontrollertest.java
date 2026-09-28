package edu.escuelaing.dosw.brasaviva.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dosw.brasaviva.dto.request.AbrirCuentaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PagoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.CuentaResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.*;
import edu.escuelaing.dosw.brasaviva.service.ICuentaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    private static final String BASE = "/api/v1/cuentas";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ICuentaService cuentaService;

    private CuentaResponseDTO abierta() {
        return new CuentaResponseDTO(1L, 3L, 0.0, "ABIERTA", LocalDateTime.now(), null, null, null, null);
    }

    private CuentaResponseDTO cerrada() {
        return new CuentaResponseDTO(1L, 3L, 110000.0, "CERRADA", LocalDateTime.now().minusHours(1),
                LocalDateTime.now(), "EFECTIVO", 120000.0, 10000.0);
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    @DisplayName("POST /cuentas - 201 al abrir cuenta")
    void abrir_valido_debeRetornar201() throws Exception {
        when(cuentaService.abrir(any(AbrirCuentaRequestDTO.class))).thenReturn(abierta());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(new AbrirCuentaRequestDTO(3L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTA"));
    }

    @Test
    @DisplayName("POST /cuentas - 400 cuando falta idMesa")
    void abrir_sinMesa_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("El id de la mesa es obligatorio")));

        verifyNoInteractions(cuentaService);
    }

    @Test
    @DisplayName("POST /cuentas - 404 cuando la mesa no existe")
    void abrir_mesaInexistente_debeRetornar404() throws Exception {
        when(cuentaService.abrir(any(AbrirCuentaRequestDTO.class))).thenThrow(new MesaNoEncontradaException("Mesa 3 no existe"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(new AbrirCuentaRequestDTO(3L))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /cuentas - 422 cuando la mesa ya tiene cuenta abierta (RN-03)")
    void abrir_yaAbierta_debeRetornar422() throws Exception {
        when(cuentaService.abrir(any(AbrirCuentaRequestDTO.class))).thenThrow(new CuentaYaAbiertaException("Mesa 3 ya tiene cuenta"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(new AbrirCuentaRequestDTO(3L))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Mesa 3 ya tiene cuenta"));
    }

    @Test
    @DisplayName("GET /cuentas/{id} - 200 cuando existe")
    void obtenerPorId_existente_debeRetornar200() throws Exception {
        when(cuentaService.obtenerPorId(1L)).thenReturn(abierta());

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMesa").value(3));
    }

    @Test
    @DisplayName("GET /cuentas/{id} - 404 cuando no existe")
    void obtenerPorId_noExiste_debeRetornar404() throws Exception {
        when(cuentaService.obtenerPorId(9L)).thenThrow(new CuentaNoEncontradaException("Cuenta 9 no existe"));

        mockMvc.perform(get(BASE + "/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /cuentas/mesa/{idMesa} - 200 con la cuenta abierta")
    void obtenerAbiertaPorMesa_debeRetornar200() throws Exception {
        when(cuentaService.obtenerAbiertaPorMesa(3L)).thenReturn(abierta());

        mockMvc.perform(get(BASE + "/mesa/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ABIERTA"));
    }

    @Test
    @DisplayName("GET /cuentas/mesa/{idMesa} - 404 cuando la mesa no tiene cuenta abierta")
    void obtenerAbiertaPorMesa_sinCuenta_debeRetornar404() throws Exception {
        when(cuentaService.obtenerAbiertaPorMesa(3L)).thenThrow(new CuentaNoEncontradaException("Sin cuenta abierta"));

        mockMvc.perform(get(BASE + "/mesa/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /cuentas/{id}/pago - 200 al pagar")
    void registrarPago_valido_debeRetornar200() throws Exception {
        when(cuentaService.registrarPago(eq(1L), any(PagoRequestDTO.class))).thenReturn(cerrada());

        mockMvc.perform(post(BASE + "/1/pago").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PagoRequestDTO("EFECTIVO", 120000.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADA"))
                .andExpect(jsonPath("$.cambio").value(10000.0));
    }

    @Test
    @DisplayName("POST /cuentas/{id}/pago - 400 con medio de pago invalido")
    void registrarPago_medioInvalido_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE + "/1/pago").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PagoRequestDTO("CHEQUE", 50000.0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Medio de pago invalido")));
    }

    @Test
    @DisplayName("POST /cuentas/{id}/pago - 400 con monto en cero")
    void registrarPago_montoCero_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE + "/1/pago").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PagoRequestDTO("TARJETA", 0.0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("El monto debe ser mayor a cero")));
    }

    @Test
    @DisplayName("POST /cuentas/{id}/pago - 400 cuando el body viene vacio")
    void registrarPago_bodyVacio_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE + "/1/pago").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cuerpo de la peticion invalido"));
    }

    @Test
    @DisplayName("POST /cuentas/{id}/pago - 404 cuando la cuenta no existe")
    void registrarPago_cuentaInexistente_debeRetornar404() throws Exception {
        when(cuentaService.registrarPago(eq(9L), any(PagoRequestDTO.class))).thenThrow(new CuentaNoEncontradaException("Cuenta 9 no existe"));

        mockMvc.perform(post(BASE + "/9/pago").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PagoRequestDTO("EFECTIVO", 1000.0))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /cuentas/{id}/pago - 422 cuando el pago es insuficiente")
    void registrarPago_insuficiente_debeRetornar422() throws Exception {
        when(cuentaService.registrarPago(eq(1L), any(PagoRequestDTO.class))).thenThrow(new PagoInsuficienteException("Monto insuficiente"));

        mockMvc.perform(post(BASE + "/1/pago").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PagoRequestDTO("EFECTIVO", 1.0))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /cuentas/{id}/pago - 422 cuando hay pedidos pendientes")
    void registrarPago_pedidosPendientes_debeRetornar422() throws Exception {
        when(cuentaService.registrarPago(eq(1L), any(PagoRequestDTO.class)))
                .thenThrow(new CuentaConPedidosPendientesException("Hay pedidos sin entregar"));

        mockMvc.perform(post(BASE + "/1/pago").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PagoRequestDTO("TARJETA", 200000.0))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Hay pedidos sin entregar"));
    }
}