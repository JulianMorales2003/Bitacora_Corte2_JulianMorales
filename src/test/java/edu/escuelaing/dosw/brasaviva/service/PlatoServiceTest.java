package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.DisponibilidadRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PlatoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.MenuItemResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.PlatoNoEncontradoException;
import edu.escuelaing.dosw.brasaviva.exception.PlatoYaExisteException;
import edu.escuelaing.dosw.brasaviva.mapper.in.PlatoMapperIn;
import edu.escuelaing.dosw.brasaviva.mapper.out.PlatoMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import edu.escuelaing.dosw.brasaviva.service.impl.PlatoServiceImpl;
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
class PlatoServiceTest {

    @Mock
    private PlatoMapperIn mapperIn;

    @Mock
    private PlatoMapperOut mapperOut;

    @InjectMocks
    private PlatoServiceImpl platoService;

    private PlatoRequestDTO requestDto;
    private PlatoResponseDTO responseDto;
    private Plato platoEntidad;

    @BeforeEach
    void setUp() {
        requestDto = new PlatoRequestDTO("Picanha", 58000.0, "CORTE", "300 g a la brasa", 20);
        platoEntidad = new Plato(null, "Picanha", 58000.0, "CORTE", "300 g a la brasa", null, 20);
        responseDto = new PlatoResponseDTO(1L, "Picanha", 58000.0, "CORTE", "300 g a la brasa", true, 20);
    }

    private void mappersQueCopian() {
        lenient().when(mapperIn.toDomain(any(PlatoRequestDTO.class))).thenAnswer(inv -> {
            PlatoRequestDTO d = inv.getArgument(0);
            return new Plato(null, d.nombre(), d.precio(), d.categoria(), d.descripcion(), null, d.tiempoPreparacionMin());
        });
        lenient().when(mapperOut.toDTO(any(Plato.class))).thenAnswer(inv -> {
            Plato p = inv.getArgument(0);
            return new PlatoResponseDTO(p.getId(), p.getNombre(), p.getPrecio(), p.getCategoria(),
                    p.getDescripcion(), p.getDisponible(), p.getTiempoPreparacionMin());
        });
        lenient().when(mapperOut.toMenuDTO(any(Plato.class))).thenAnswer(inv -> {
            Plato p = inv.getArgument(0);
            return new MenuItemResponseDTO(p.getId(), p.getNombre(), p.getPrecio(), p.getCategoria(), p.getDescripcion());
        });
    }

    private PlatoRequestDTO plato(String nombre, String categoria) {
        return new PlatoRequestDTO(nombre, 10000.0, categoria, null, 10);
    }

    @Test
    @DisplayName("Crear plato valido - debe retornar ResponseDTO con id asignado")
    void crear_platoValido_debeRetornarResponseDTO() {
        when(mapperIn.toDomain(requestDto)).thenReturn(platoEntidad);
        when(mapperOut.toDTO(any(Plato.class))).thenReturn(responseDto);

        PlatoResponseDTO resultado = platoService.crear(requestDto);

        assertNotNull(resultado);
        assertEquals("Picanha", resultado.nombre());
        assertEquals(58000.0, resultado.precio());
        assertEquals(1L, platoEntidad.getId());
        assertTrue(platoEntidad.getDisponible());
        verify(mapperIn, times(1)).toDomain(requestDto);
        verify(mapperOut, times(1)).toDTO(any(Plato.class));
    }

    @Test
    @DisplayName("Crear plato con nombre duplicado (sin importar mayusculas) - PlatoYaExisteException")
    void crear_nombreDuplicado_debeLanzarExcepcion() {
        mappersQueCopian();
        platoService.crear(requestDto);

        PlatoRequestDTO duplicado = new PlatoRequestDTO("PICANHA", 60000.0, "CORTE", null, 20);
        PlatoYaExisteException ex = assertThrows(PlatoYaExisteException.class,
                () -> platoService.crear(duplicado));

        assertTrue(ex.getMessage().contains("PICANHA"));
        verify(mapperIn, times(1)).toDomain(any(PlatoRequestDTO.class));
    }

    @Test
    @DisplayName("Obtener todos sin platos - lista vacia, no null")
    void obtenerTodos_sinPlatos_debeRetornarListaVacia() {
        List<PlatoResponseDTO> resultado = platoService.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(mapperOut, never()).toDTO(any(Plato.class));
    }

    @Test
    @DisplayName("Obtener plato inexistente - PlatoNoEncontradoException")
    void obtenerPorId_inexistente_debeLanzarExcepcion() {
        PlatoNoEncontradoException ex = assertThrows(PlatoNoEncontradoException.class,
                () -> platoService.obtenerPorId(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    @DisplayName("Obtener disponibles - excluye los agotados")
    void obtenerDisponibles_debeExcluirAgotados() {
        mappersQueCopian();
        platoService.crear(plato("Churrasco", "CORTE"));
        PlatoResponseDTO agotado = platoService.crear(plato("Chorizo", "ENTRADA"));
        platoService.cambiarDisponibilidad(agotado.id(), new DisponibilidadRequestDTO(false));

        List<PlatoResponseDTO> disponibles = platoService.obtenerDisponibles();

        assertEquals(1, disponibles.size());
        assertEquals("Churrasco", disponibles.get(0).nombre());
        assertEquals(2, platoService.obtenerTodos().size());
    }

    @Test
    @DisplayName("Actualizar plato - cambia sus datos")
    void actualizar_platoExistente_debeCambiarDatos() {
        mappersQueCopian();
        PlatoResponseDTO creado = platoService.crear(requestDto);

        PlatoResponseDTO actualizado = platoService.actualizar(creado.id(),
                new PlatoRequestDTO("Picanha premium", 65000.0, "CORTE", "400 g", 22));

        assertEquals("Picanha premium", actualizado.nombre());
        assertEquals(65000.0, actualizado.precio());
        assertEquals(22, actualizado.tiempoPreparacionMin());
    }

    @Test
    @DisplayName("Actualizar con el nombre de otro plato - PlatoYaExisteException")
    void actualizar_nombreDeOtroPlato_debeLanzarExcepcion() {
        mappersQueCopian();
        platoService.crear(plato("Churrasco", "CORTE"));
        PlatoResponseDTO chorizo = platoService.crear(plato("Chorizo", "ENTRADA"));

        assertThrows(PlatoYaExisteException.class,
                () -> platoService.actualizar(chorizo.id(), plato("churrasco", "ENTRADA")));
    }

    @Test
    @DisplayName("Actualizar conservando su propio nombre - no es duplicado")
    void actualizar_mismoNombre_noEsDuplicado() {
        mappersQueCopian();
        PlatoResponseDTO creado = platoService.crear(requestDto);

        assertDoesNotThrow(() -> platoService.actualizar(creado.id(), requestDto));
    }

    @Test
    @DisplayName("Actualizar plato inexistente - PlatoNoEncontradoException")
    void actualizar_inexistente_debeLanzarExcepcion() {
        assertThrows(PlatoNoEncontradoException.class, () -> platoService.actualizar(5L, requestDto));
    }

    @Test
    @DisplayName("Marcar plato como agotado - disponible queda en false")
    void cambiarDisponibilidad_debeMarcarAgotado() {
        mappersQueCopian();
        PlatoResponseDTO creado = platoService.crear(requestDto);

        PlatoResponseDTO resultado = platoService.cambiarDisponibilidad(creado.id(), new DisponibilidadRequestDTO(false));

        assertFalse(resultado.disponible());
        assertFalse(platoService.obtenerEntidad(creado.id()).estaDisponible());
    }

    @Test
    @DisplayName("Eliminar plato - deja de existir")
    void eliminar_platoExistente_debeBorrarlo() {
        mappersQueCopian();
        PlatoResponseDTO creado = platoService.crear(requestDto);

        platoService.eliminar(creado.id());

        assertThrows(PlatoNoEncontradoException.class, () -> platoService.obtenerPorId(creado.id()));
    }

    @Test
    @DisplayName("Eliminar plato inexistente - PlatoNoEncontradoException")
    void eliminar_inexistente_debeLanzarExcepcion() {
        assertThrows(PlatoNoEncontradoException.class, () -> platoService.eliminar(7L));
    }

    @Test
    @DisplayName("Menu por categoria - solo disponibles de esa categoria")
    void obtenerMenu_porCategoria_debeFiltrar() {
        mappersQueCopian();
        platoService.crear(plato("Picanha", "CORTE"));
        PlatoResponseDTO tomahawk = platoService.crear(plato("Tomahawk", "CORTE"));
        platoService.crear(plato("Limonada", "BEBIDA"));
        platoService.cambiarDisponibilidad(tomahawk.id(), new DisponibilidadRequestDTO(false));

        List<MenuItemResponseDTO> cortes = platoService.obtenerMenu("corte");

        assertEquals(1, cortes.size());
        assertEquals("Picanha", cortes.get(0).nombre());
        assertEquals(2, platoService.obtenerMenu(null).size());
    }

    @Test
    @DisplayName("Ver en el menu un plato agotado - el cliente no lo encuentra")
    void obtenerItemMenu_agotado_debeLanzarExcepcion() {
        mappersQueCopian();
        PlatoResponseDTO creado = platoService.crear(requestDto);
        platoService.cambiarDisponibilidad(creado.id(), new DisponibilidadRequestDTO(false));

        assertThrows(PlatoNoEncontradoException.class, () -> platoService.obtenerItemMenu(creado.id()));
    }
}