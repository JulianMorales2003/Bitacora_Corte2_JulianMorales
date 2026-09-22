package edu.escuelaing.dosw.brasaviva.mapper;

import edu.escuelaing.dosw.brasaviva.dto.request.PlatoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.MenuItemResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoResponseDTO;
import edu.escuelaing.dosw.brasaviva.mapper.in.PlatoMapperInImpl;
import edu.escuelaing.dosw.brasaviva.mapper.out.PlatoMapperOutImpl;
import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlatoMapperTest {

    private final PlatoMapperInImpl mapperIn = new PlatoMapperInImpl();
    private final PlatoMapperOutImpl mapperOut = new PlatoMapperOutImpl();

    private PlatoRequestDTO requestDto;
    private Plato plato;

    @BeforeEach
    void setUp() {
        requestDto = new PlatoRequestDTO("Picanha", 58000.0, "CORTE", "300 g a la brasa", 20);
        plato = new Plato(1L, "Picanha", 58000.0, "CORTE", "300 g a la brasa", true, 20);
    }

    @Test
    @DisplayName("toDomain: copia todos los campos del request, sin id ni disponible")
    void toDomain_debeCopiarCampos() {
        Plato resultado = mapperIn.toDomain(requestDto);

        assertNull(resultado.getId());
        assertNull(resultado.getDisponible());
        assertEquals("Picanha", resultado.getNombre());
        assertEquals(58000.0, resultado.getPrecio());
        assertEquals("CORTE", resultado.getCategoria());
        assertEquals("300 g a la brasa", resultado.getDescripcion());
        assertEquals(20, resultado.getTiempoPreparacionMin());
    }

    @Test
    @DisplayName("toDomain: con un DTO nulo retorna null")
    void toDomain_dtoNulo_debeRetornarNull() {
        assertNull(mapperIn.toDomain(null));
    }

    @Test
    @DisplayName("toDTO (PlatoResponseDTO): copia todos los campos del dominio")
    void toDTO_debeCopiarCampos() {
        PlatoResponseDTO resultado = mapperOut.toDTO(plato);

        assertEquals(1L, resultado.id());
        assertEquals("Picanha", resultado.nombre());
        assertEquals(58000.0, resultado.precio());
        assertEquals("CORTE", resultado.categoria());
        assertEquals("300 g a la brasa", resultado.descripcion());
        assertTrue(resultado.disponible());
        assertEquals(20, resultado.tiempoPreparacionMin());
    }

    @Test
    @DisplayName("toMenuDTO: no expone disponible ni tiempoPreparacionMin")
    void toMenuDTO_debeOmitirCamposInternos() {
        MenuItemResponseDTO resultado = mapperOut.toMenuDTO(plato);

        assertEquals(1L, resultado.id());
        assertEquals("Picanha", resultado.nombre());
        assertEquals(58000.0, resultado.precio());
        assertEquals("CORTE", resultado.categoria());
        assertEquals("300 g a la brasa", resultado.descripcion());
    }

    @Test
    @DisplayName("toDTO: con dominio nulo retorna null")
    void toDTO_dominioNulo_debeRetornarNull() {
        assertNull(mapperOut.toDTO(null));
    }
}