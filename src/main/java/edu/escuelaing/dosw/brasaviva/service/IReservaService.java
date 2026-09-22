package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.ReservaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ReservaResponseDTO;

import java.util.List;

public interface IReservaService {

    ReservaResponseDTO crear(ReservaRequestDTO dto);

    List<ReservaResponseDTO> obtenerTodas(String cliente);

    ReservaResponseDTO obtenerPorId(Long id);

    ReservaResponseDTO actualizar(Long id, ReservaRequestDTO dto);

    void cancelar(Long id);
}