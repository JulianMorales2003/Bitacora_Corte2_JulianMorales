package edu.escuelaing.dosw.brasaviva.model;

import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import edu.escuelaing.dosw.brasaviva.model.domain.Reserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModeloDominioTest {

    @Test
    @DisplayName("Transiciones validas del pedido")
    void estadoPedido_transicionesValidas() {
        assertTrue(EstadoPedido.RECIBIDO.puedeTransicionarA(EstadoPedido.EN_PREPARACION));
        assertTrue(EstadoPedido.RECIBIDO.puedeTransicionarA(EstadoPedido.CANCELADO));
        assertTrue(EstadoPedido.EN_PREPARACION.puedeTransicionarA(EstadoPedido.LISTO));
        assertTrue(EstadoPedido.LISTO.puedeTransicionarA(EstadoPedido.ENTREGADO));
    }

    @Test
    @DisplayName("Transiciones invalidas del pedido")
    void estadoPedido_transicionesInvalidas() {
        assertFalse(EstadoPedido.RECIBIDO.puedeTransicionarA(EstadoPedido.LISTO));
        assertFalse(EstadoPedido.EN_PREPARACION.puedeTransicionarA(EstadoPedido.CANCELADO));
        assertFalse(EstadoPedido.LISTO.puedeTransicionarA(EstadoPedido.RECIBIDO));
        assertFalse(EstadoPedido.ENTREGADO.puedeTransicionarA(EstadoPedido.CANCELADO));
        assertFalse(EstadoPedido.CANCELADO.puedeTransicionarA(EstadoPedido.RECIBIDO));
    }

    @Test
    @DisplayName("Pedido: total y cortes que ocupa en la parrilla")
    void pedido_totalYCortes() {
        Pedido pedido = Pedido.builder().items(List.of(
                ItemPedido.builder().categoria("CORTE").precioCongelado(50000.0).cantidad(2).build(),
                ItemPedido.builder().categoria("BEBIDA").precioCongelado(10000.0).cantidad(3).build()
        )).build();

        assertEquals(130000.0, pedido.calcularTotal());
        assertEquals(2, pedido.contarCortes());
    }

    @Test
    @DisplayName("Plato: validez y categoria corte")
    void plato_esValidoYEsCorte() {
        Plato corte = new Plato(1L, "Picanha", 58000.0, "CORTE", null, true, 20);
        Plato invalido = new Plato(2L, " ", 0.0, "BEBIDA", null, true, 5);

        assertTrue(corte.esValido());
        assertTrue(corte.esCorte());
        assertFalse(invalido.esValido());
        assertFalse(invalido.esCorte());
    }

    @Test
    @DisplayName("Reserva: se solapa con otra a menos de 2 horas")
    void reserva_seSolapa() {
        LocalDateTime ocho = LocalDateTime.of(2026, 10, 2, 20, 0);
        Reserva reserva = Reserva.builder().fechaHora(ocho).build();

        assertTrue(reserva.seSolapaCon(ocho.plusMinutes(119)));
        assertTrue(reserva.seSolapaCon(ocho.minusMinutes(60)));
        assertFalse(reserva.seSolapaCon(ocho.plusHours(2)));
    }
}