package edu.escuelaing.dosw.brasaviva.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dosw.brasaviva.dto.request.CambioEstadoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.ItemPedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PedidoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ItemPedidoResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PedidoResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.*;
import edu.escuelaing.dosw.brasaviva.service.IPedidoService;
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

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    private static final String BASE = "/api/v1/pedidos";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private IPedidoService pedidoService;

    private PedidoResponseDTO pedido(String estado) {
        ItemPedidoResponseDTO item = new ItemPedidoResponseDTO(1L, 10L, "Picanha", 55000.0, 2, "TRES_CUARTOS", null, 110000.0);
        return new PedidoResponseDTO(1L, 3L, List.of(item), estado, LocalDateTime.now(), 110000.0);
    }

    private ItemPedidoRequestDTO itemValido() {
        return new ItemPedidoRequestDTO(10L, 2, "TRES_CUARTOS", "sin sal");
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    // ---------- POST /pedidos ----------

    @Test
    @DisplayName("POST /pedidos - 201 con pedido valido")
    void crear_valido_debeRetornar201() throws Exception {
        when(pedidoService.crear(any(PedidoRequestDTO.class))).thenReturn(pedido("RECIBIDO"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PedidoRequestDTO(3L, List.of(itemValido())))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("RECIBIDO"))
                .andExpect(jsonPath("$.items[0].nombrePlato").value("Picanha"));
    }

    @Test
    @DisplayName("POST /pedidos - 400 cuando no hay items")
    void crear_sinItems_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PedidoRequestDTO(3L, List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("El pedido debe tener al menos un plato"))));

        verifyNoInteractions(pedidoService);
    }

    @Test
    @DisplayName("POST /pedidos - 400 cuando falta la mesa")
    void crear_sinMesa_debeRetornar400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PedidoRequestDTO(null, List.of(itemValido())))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("El id de la mesa es obligatorio"))));
    }

    @Test
    @DisplayName("POST /pedidos - 400 cuando un item anidado es invalido (cantidad 0)")
    void crear_itemAnidadoInvalido_debeRetornar400() throws Exception {
        ItemPedidoRequestDTO malo = new ItemPedidoRequestDTO(10L, 0, null, null);

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PedidoRequestDTO(3L, List.of(malo)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("La cantidad minima es 1"))));
    }

    @Test
    @DisplayName("POST /pedidos - 404 cuando la mesa o el plato no existen")
    void crear_recursoInexistente_debeRetornar404() throws Exception {
        when(pedidoService.crear(any(PedidoRequestDTO.class))).thenThrow(new MesaNoEncontradaException("Mesa 3 no existe"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PedidoRequestDTO(3L, List.of(itemValido())))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /pedidos - 422 cuando el plato esta agotado (RN-02)")
    void crear_platoAgotado_debeRetornar422() throws Exception {
        when(pedidoService.crear(any(PedidoRequestDTO.class))).thenThrow(new PlatoNoDisponibleException("Picanha agotado"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PedidoRequestDTO(3L, List.of(itemValido())))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("Picanha agotado"));
    }

    @Test
    @DisplayName("POST /pedidos - 422 cuando el corte no trae termino (RN-P01)")
    void crear_corteSinTermino_debeRetornar422() throws Exception {
        when(pedidoService.crear(any(PedidoRequestDTO.class)))
                .thenThrow(new TerminoCoccionRequeridoException("El corte requiere termino de coccion"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PedidoRequestDTO(3L, List.of(new ItemPedidoRequestDTO(10L, 1, null, null))))))
                .andExpect(status().isUnprocessableEntity());
    }

    // ---------- GET ----------

    @Test
    @DisplayName("GET /pedidos - 200 y filtro por estado")
    void obtenerTodos_conFiltro_debeRetornar200() throws Exception {
        when(pedidoService.obtenerTodos("LISTO")).thenReturn(List.of(pedido("LISTO")));

        mockMvc.perform(get(BASE).param("estado", "LISTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("LISTO"));

        verify(pedidoService).obtenerTodos("LISTO");
    }

    @Test
    @DisplayName("GET /pedidos/cocina - 200 tablero de cocina")
    void tableroCocina_debeRetornar200() throws Exception {
        when(pedidoService.obtenerTableroCocina()).thenReturn(List.of(pedido("RECIBIDO"), pedido("EN_PREPARACION")));

        mockMvc.perform(get(BASE + "/cocina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /pedidos/mesa/{id} - 200 pedidos activos")
    void activosPorMesa_debeRetornar200() throws Exception {
        when(pedidoService.obtenerActivosPorMesa(3L)).thenReturn(List.of(pedido("RECIBIDO")));

        mockMvc.perform(get(BASE + "/mesa/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idMesa").value(3));
    }

    @Test
    @DisplayName("GET /pedidos/mesa/{id} - 404 cuando la mesa no existe")
    void activosPorMesa_mesaInexistente_debeRetornar404() throws Exception {
        when(pedidoService.obtenerActivosPorMesa(99L)).thenThrow(new MesaNoEncontradaException("Mesa 99 no existe"));

        mockMvc.perform(get(BASE + "/mesa/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /pedidos/{id} - 200 cuando existe")
    void obtenerPorId_existente_debeRetornar200() throws Exception {
        when(pedidoService.obtenerPorId(1L)).thenReturn(pedido("RECIBIDO"));

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(110000.0));
    }

    @Test
    @DisplayName("GET /pedidos/{id} - 404 cuando no existe")
    void obtenerPorId_noExiste_debeRetornar404() throws Exception {
        when(pedidoService.obtenerPorId(9L)).thenThrow(new PedidoNoEncontradoException("Pedido 9 no existe"));

        mockMvc.perform(get(BASE + "/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pedido 9 no existe"));
    }

    // ---------- POST /pedidos/{id}/items ----------

    @Test
    @DisplayName("POST /pedidos/{id}/items - 200 al agregar item")
    void agregarItem_valido_debeRetornar200() throws Exception {
        when(pedidoService.agregarItem(eq(1L), any(ItemPedidoRequestDTO.class))).thenReturn(pedido("RECIBIDO"));

        mockMvc.perform(post(BASE + "/1/items").contentType(MediaType.APPLICATION_JSON).content(json(itemValido())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /pedidos/{id}/items - 400 con termino de coccion invalido")
    void agregarItem_terminoInvalido_debeRetornar400() throws Exception {
        ItemPedidoRequestDTO dto = new ItemPedidoRequestDTO(10L, 1, "CARBONIZADO", null);

        mockMvc.perform(post(BASE + "/1/items").contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Termino invalido"))));
    }

    @Test
    @DisplayName("POST /pedidos/{id}/items - 400 cuando la cantidad supera 20")
    void agregarItem_cantidadExcesiva_debeRetornar400() throws Exception {
        ItemPedidoRequestDTO dto = new ItemPedidoRequestDTO(10L, 21, null, null);

        mockMvc.perform(post(BASE + "/1/items").contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("La cantidad maxima por item es 20"))));
    }

    @Test
    @DisplayName("POST /pedidos/{id}/items - 422 cuando el pedido ya esta en cocina (RN-01)")
    void agregarItem_pedidoNoModificable_debeRetornar422() throws Exception {
        when(pedidoService.agregarItem(eq(1L), any(ItemPedidoRequestDTO.class)))
                .thenThrow(new PedidoNoModificableException("El pedido ya esta en cocina"));

        mockMvc.perform(post(BASE + "/1/items").contentType(MediaType.APPLICATION_JSON).content(json(itemValido())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("El pedido ya esta en cocina"));
    }

    // ---------- PATCH /pedidos/{id}/estado ----------

    @Test
    @DisplayName("PATCH /pedidos/{id}/estado - 200 al cambiar estado")
    void cambiarEstado_valido_debeRetornar200() throws Exception {
        when(pedidoService.cambiarEstado(eq(1L), any(CambioEstadoRequestDTO.class))).thenReturn(pedido("EN_PREPARACION"));

        mockMvc.perform(patch(BASE + "/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CambioEstadoRequestDTO("EN_PREPARACION"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"));
    }

    @Test
    @DisplayName("PATCH /pedidos/{id}/estado - 400 con estado inexistente")
    void cambiarEstado_estadoInvalido_debeRetornar400() throws Exception {
        mockMvc.perform(patch(BASE + "/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CambioEstadoRequestDTO("VOLANDO"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Estado invalido"))));

        verifyNoInteractions(pedidoService);
    }

    @Test
    @DisplayName("PATCH /pedidos/{id}/estado - 400 con estado en blanco")
    void cambiarEstado_estadoVacio_debeRetornar400() throws Exception {
        mockMvc.perform(patch(BASE + "/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /pedidos/{id}/estado - 404 cuando el pedido no existe")
    void cambiarEstado_pedidoInexistente_debeRetornar404() throws Exception {
        when(pedidoService.cambiarEstado(eq(9L), any(CambioEstadoRequestDTO.class)))
                .thenThrow(new PedidoNoEncontradoException("Pedido 9 no existe"));

        mockMvc.perform(patch(BASE + "/9/estado").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CambioEstadoRequestDTO("LISTO"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /pedidos/{id}/estado - 422 transicion invalida")
    void cambiarEstado_transicionInvalida_debeRetornar422() throws Exception {
        when(pedidoService.cambiarEstado(eq(1L), any(CambioEstadoRequestDTO.class)))
                .thenThrow(new EstadoInvalidoException("No se puede pasar de RECIBIDO a ENTREGADO"));

        mockMvc.perform(patch(BASE + "/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CambioEstadoRequestDTO("ENTREGADO"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("PATCH /pedidos/{id}/estado - 422 cuando la parrilla esta llena (RN-P02)")
    void cambiarEstado_parrillaLlena_debeRetornar422() throws Exception {
        when(pedidoService.cambiarEstado(eq(1L), any(CambioEstadoRequestDTO.class)))
                .thenThrow(new CapacidadParrillaExcedidaException("Parrilla llena"));

        mockMvc.perform(patch(BASE + "/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CambioEstadoRequestDTO("EN_PREPARACION"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Parrilla llena"));
    }

    // ---------- DELETE ----------

    @Test
    @DisplayName("DELETE /pedidos/{id} - 204 al cancelar")
    void cancelar_valido_debeRetornar204() throws Exception {
        doNothing().when(pedidoService).cancelar(1L);

        mockMvc.perform(delete(BASE + "/1")).andExpect(status().isNoContent());

        verify(pedidoService).cancelar(1L);
    }

    @Test
    @DisplayName("DELETE /pedidos/{id} - 404 cuando no existe")
    void cancelar_noExiste_debeRetornar404() throws Exception {
        doThrow(new PedidoNoEncontradoException("Pedido 9 no existe")).when(pedidoService).cancelar(9L);

        mockMvc.perform(delete(BASE + "/9")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /pedidos/{id} - 422 cuando ya esta en cocina")
    void cancelar_enCocina_debeRetornar422() throws Exception {
        doThrow(new PedidoNoModificableException("Ya esta en cocina")).when(pedidoService).cancelar(1L);

        mockMvc.perform(delete(BASE + "/1")).andExpect(status().isUnprocessableEntity());
    }
}