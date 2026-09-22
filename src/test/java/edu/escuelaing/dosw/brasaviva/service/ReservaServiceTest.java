package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.ReservaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ReservaResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.CapacidadMesaExcedidaException;
import edu.escuelaing.dosw.brasaviva.exception.EstadoInvalidoException;
import edu.escuelaing.dosw.brasaviva.exception.MesaNoEncontradaException;
import edu.escuelaing.dosw.brasaviva.exception.ReservaConflictoException;
import edu.escuelaing.dosw.brasaviva.exception.ReservaNoEncontradaException;
import edu.escuelaing.dosw.brasaviva.mapper.in.ReservaMapperIn;
import edu.escuelaing.dosw.brasaviva.mapper.out.ReservaMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoMesa;
import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import edu.escuelaing.dosw.brasaviva.model.domain.Reserva;
import edu.escuelaing.dosw.brasaviva.service.impl.ReservaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    private static final LocalDateTime VIERNES_8PM = LocalDateTime.now().plusDays(7).withHour(20).withMinute(0).withSecond(0).withNano(0);

    @Mock
    private IMesaService mesaService;

    @Mock
    private ReservaMapperIn mapperIn;

    @Mock
    private ReservaMapperOut mapperOut;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    @BeforeEach
    void setUp() {
        lenient().when(mesaService.obtenerEntidad(1L)).thenReturn(new Mesa(1L, 1, 4, EstadoMesa.DISPONIBLE, false));
        lenient().when(mesaService.obtenerEntidad(2L)).thenReturn(new Mesa(2L, 2, 8, EstadoMesa.DISPONIBLE, false));
        lenient().when(mapperIn.toDomain(any(ReservaRequestDTO.class))).thenAnswer(inv -> {
            ReservaRequestDTO d = inv.getArgument(0);
            return Reserva.builder().idMesa(d.idMesa()).cliente(d.cliente())
                    .fechaHora(d.fechaHora()).comensales(d.comensales()).build();
        });
        lenient().when(mapperOut.toDTO(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            return new ReservaResponseDTO(r.getId(), r.getIdMesa(), r.getCliente(), r.getFechaHora(),
                    r.getComensales(), r.getEstado().name());
        });
    }

    private ReservaRequestDTO reserva(Long idMesa, String cliente, LocalDateTime fecha, int comensales) {
        return new ReservaRequestDTO(idMesa, cliente, fecha, comensales);
    }

    @Test
    @DisplayName("Crear reserva valida - queda ACTIVA")
    void crear_reservaValida_debeQuedarActiva() {
        ReservaResponseDTO resultado = reservaService.crear(reserva(1L, "Laura", VIERNES_8PM, 4));

        assertEquals(1L, resultado.id());
        assertEquals("ACTIVA", resultado.estado());
        verify(mapperIn, times(1)).toDomain(any(ReservaRequestDTO.class));
    }

    @Test
    @DisplayName("Reservar para mas personas que la capacidad - CapacidadMesaExcedidaException")
    void crear_superaCapacidad_debeLanzarExcepcion() {
        assertThrows(CapacidadMesaExcedidaException.class,
                () -> reservaService.crear(reserva(1L, "Laura", VIERNES_8PM, 6)));
    }

    @Test
    @DisplayName("Reservar en mesa inexistente - propaga MesaNoEncontradaException")
    void crear_mesaInexistente_debeLanzarExcepcion() {
        when(mesaService.obtenerEntidad(9L)).thenThrow(new MesaNoEncontradaException("Mesa no encontrada: 9"));

        assertThrows(MesaNoEncontradaException.class,
                () -> reservaService.crear(reserva(9L, "Laura", VIERNES_8PM, 2)));
    }

    @Test
    @DisplayName("Misma mesa con menos de 2 horas de diferencia - ReservaConflictoException")
    void crear_choqueDeHorario_debeLanzarExcepcion() {
        reservaService.crear(reserva(1L, "Laura", VIERNES_8PM, 4));

        assertThrows(ReservaConflictoException.class,
                () -> reservaService.crear(reserva(1L, "Pedro", VIERNES_8PM.plusMinutes(90), 2)));
    }

    @Test
    @DisplayName("Misma mesa con 2 horas o mas de diferencia - es valida")
    void crear_sinChoque_esValida() {
        reservaService.crear(reserva(1L, "Laura", VIERNES_8PM, 4));

        assertDoesNotThrow(() -> reservaService.crear(reserva(1L, "Pedro", VIERNES_8PM.plusHours(2), 2)));
        assertDoesNotThrow(() -> reservaService.crear(reserva(2L, "Ana", VIERNES_8PM, 6)));
    }

    @Test
    @DisplayName("Una reserva cancelada libera el horario")
    void crear_despuesDeCancelar_esValida() {
        ReservaResponseDTO laura = reservaService.crear(reserva(1L, "Laura", VIERNES_8PM, 4));
        reservaService.cancelar(laura.id());

        assertDoesNotThrow(() -> reservaService.crear(reserva(1L, "Pedro", VIERNES_8PM, 2)));
    }

    @Test
    @DisplayName("Reprogramar una reserva a una hora cercana no choca consigo misma")
    void actualizar_mismaReserva_noChocaConsigoMisma() {
        ReservaResponseDTO laura = reservaService.crear(reserva(1L, "Laura", VIERNES_8PM, 4));

        ReservaResponseDTO resultado = reservaService.actualizar(laura.id(),
                reserva(1L, "Laura", VIERNES_8PM.plusMinutes(30), 3));

        assertEquals(VIERNES_8PM.plusMinutes(30), resultado.fechaHora());
        assertEquals(3, resultado.comensales());
    }

    @Test
    @DisplayName("Actualizar una reserva cancelada - EstadoInvalidoException")
    void actualizar_reservaCancelada_debeLanzarExcepcion() {
        ReservaResponseDTO laura = reservaService.crear(reserva(1L, "Laura", VIERNES_8PM, 4));
        reservaService.cancelar(laura.id());

        assertThrows(EstadoInvalidoException.class,
                () -> reservaService.actualizar(laura.id(), reserva(1L, "Laura", VIERNES_8PM, 2)));
    }

    @Test
    @DisplayName("Cancelar dos veces - EstadoInvalidoException")
    void cancelar_dosVeces_debeLanzarExcepcion() {
        ReservaResponseDTO laura = reservaService.crear(reserva(1L, "Laura", VIERNES_8PM, 4));
        reservaService.cancelar(laura.id());

        assertThrows(EstadoInvalidoException.class, () -> reservaService.cancelar(laura.id()));
        assertEquals("CANCELADA", reservaService.obtenerPorId(laura.id()).estado());
    }

    @Test
    @DisplayName("Reserva inexistente - ReservaNoEncontradaException")
    void obtenerPorId_inexistente_debeLanzarExcepcion() {
        assertThrows(ReservaNoEncontradaException.class, () -> reservaService.obtenerPorId(4L));
        assertThrows(ReservaNoEncontradaException.class, () -> reservaService.cancelar(4L));
    }

    @Test
    @DisplayName("Consultar por cliente - filtra por nombre sin importar mayusculas")
    void obtenerTodas_porCliente_debeFiltrar() {
        reservaService.crear(reserva(1L, "Laura Gomez", VIERNES_8PM, 4));
        reservaService.crear(reserva(2L, "Pedro Ruiz", VIERNES_8PM, 4));

        List<ReservaResponseDTO> deLaura = reservaService.obtenerTodas("laura");

        assertEquals(1, deLaura.size());
        assertEquals("Laura Gomez", deLaura.get(0).cliente());
        assertEquals(2, reservaService.obtenerTodas(null).size());
    }

    @Test
    @DisplayName("Sin reservas - lista vacia")
    void obtenerTodas_sinReservas_debeRetornarListaVacia() {
        assertTrue(reservaService.obtenerTodas(null).isEmpty());
    }
}