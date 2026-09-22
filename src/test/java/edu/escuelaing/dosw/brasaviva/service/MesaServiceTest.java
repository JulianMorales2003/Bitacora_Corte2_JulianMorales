package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.MesaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.MesaResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.CuentaYaAbiertaException;
import edu.escuelaing.dosw.brasaviva.exception.MesaNoEncontradaException;
import edu.escuelaing.dosw.brasaviva.exception.MesaYaExisteException;
import edu.escuelaing.dosw.brasaviva.mapper.in.MesaMapperIn;
import edu.escuelaing.dosw.brasaviva.mapper.out.MesaMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoMesa;
import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import edu.escuelaing.dosw.brasaviva.service.impl.MesaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MesaServiceTest {

    @Mock
    private MesaMapperIn mapperIn;

    @Mock
    private MesaMapperOut mapperOut;

    @InjectMocks
    private MesaServiceImpl mesaService;

    @BeforeEach
    void setUp() {
        lenient().when(mapperIn.toDomain(any(MesaRequestDTO.class))).thenAnswer(inv -> {
            MesaRequestDTO d = inv.getArgument(0);
            return Mesa.builder().numero(d.numero()).capacidad(d.capacidad()).build();
        });
        lenient().when(mapperOut.toDTO(any(Mesa.class))).thenAnswer(inv -> {
            Mesa m = inv.getArgument(0);
            return new MesaResponseDTO(m.getId(), m.getNumero(), m.getCapacidad(), m.getEstado().name(), m.getCuentaAbierta());
        });
    }

    @Test
    @DisplayName("Crear mesa - queda DISPONIBLE y sin cuenta")
    void crear_mesaValida_debeQuedarDisponible() {
        MesaResponseDTO resultado = mesaService.crear(new MesaRequestDTO(1, 4));

        assertEquals(1L, resultado.id());
        assertEquals("DISPONIBLE", resultado.estado());
        assertFalse(resultado.cuentaAbierta());
        verify(mapperIn, times(1)).toDomain(any(MesaRequestDTO.class));
    }

    @Test
    @DisplayName("Crear mesa con numero repetido - MesaYaExisteException")
    void crear_numeroRepetido_debeLanzarExcepcion() {
        mesaService.crear(new MesaRequestDTO(1, 4));

        assertThrows(MesaYaExisteException.class, () -> mesaService.crear(new MesaRequestDTO(1, 6)));
    }

    @Test
    @DisplayName("Obtener mesa inexistente - MesaNoEncontradaException")
    void obtenerPorId_inexistente_debeLanzarExcepcion() {
        assertThrows(MesaNoEncontradaException.class, () -> mesaService.obtenerPorId(10L));
    }

    @Test
    @DisplayName("Sin mesas - lista vacia")
    void obtenerTodas_sinMesas_debeRetornarListaVacia() {
        assertTrue(mesaService.obtenerTodas(null).isEmpty());
    }

    @Test
    @DisplayName("Abrir cuenta - la mesa queda OCUPADA")
    void abrirCuenta_mesaLibre_debeQuedarOcupada() {
        MesaResponseDTO mesa = mesaService.crear(new MesaRequestDTO(3, 4));

        mesaService.abrirCuenta(mesa.id());

        Mesa entidad = mesaService.obtenerEntidad(mesa.id());
        assertTrue(entidad.tieneCuentaAbierta());
        assertEquals(EstadoMesa.OCUPADA, entidad.getEstado());
        assertEquals(1, mesaService.contarConCuentaAbierta());
    }

    @Test
    @DisplayName("RN-03: abrir una segunda cuenta en la misma mesa - CuentaYaAbiertaException")
    void abrirCuenta_yaAbierta_debeLanzarExcepcion() {
        MesaResponseDTO mesa = mesaService.crear(new MesaRequestDTO(3, 4));
        mesaService.abrirCuenta(mesa.id());

        assertThrows(CuentaYaAbiertaException.class, () -> mesaService.abrirCuenta(mesa.id()));
    }

    @Test
    @DisplayName("Cerrar cuenta - la mesa vuelve a DISPONIBLE")
    void cerrarCuenta_debeLiberarMesa() {
        MesaResponseDTO mesa = mesaService.crear(new MesaRequestDTO(3, 4));
        mesaService.abrirCuenta(mesa.id());

        mesaService.cerrarCuenta(mesa.id());

        assertTrue(mesaService.obtenerEntidad(mesa.id()).estaDisponible());
        assertEquals(0, mesaService.contarConCuentaAbierta());
    }

    @Test
    @DisplayName("Filtrar por estado - solo devuelve las mesas en ese estado")
    void obtenerTodas_filtroEstado_debeFiltrar() {
        MesaResponseDTO m1 = mesaService.crear(new MesaRequestDTO(1, 4));
        mesaService.crear(new MesaRequestDTO(2, 4));
        mesaService.abrirCuenta(m1.id());

        List<MesaResponseDTO> ocupadas = mesaService.obtenerTodas("ocupada");

        assertEquals(1, ocupadas.size());
        assertEquals(1, ocupadas.get(0).numero());
        assertEquals(2, mesaService.obtenerTodas(null).size());
    }
}