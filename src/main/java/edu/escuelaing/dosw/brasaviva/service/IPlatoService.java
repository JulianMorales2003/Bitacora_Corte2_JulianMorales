package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.DisponibilidadRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PlatoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.MenuItemResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoResponseDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Plato;

import java.util.List;

public interface IPlatoService {

    List<PlatoResponseDTO> obtenerTodos();

    List<PlatoResponseDTO> obtenerDisponibles();

    PlatoResponseDTO obtenerPorId(Long id);

    PlatoResponseDTO crear(PlatoRequestDTO dto);

    PlatoResponseDTO actualizar(Long id, PlatoRequestDTO dto);

    PlatoResponseDTO cambiarDisponibilidad(Long id, DisponibilidadRequestDTO dto);

    void eliminar(Long id);

    List<MenuItemResponseDTO> obtenerMenu(String categoria);

    MenuItemResponseDTO obtenerItemMenu(Long id);

    Plato obtenerEntidad(Long id);
}