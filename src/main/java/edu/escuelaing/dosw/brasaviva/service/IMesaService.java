package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.MesaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.MesaResponseDTO;
import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;

import java.util.List;

public interface IMesaService {

    List<MesaResponseDTO> obtenerTodas(String estado);

    MesaResponseDTO obtenerPorId(Long id);

    MesaResponseDTO crear(MesaRequestDTO dto);

    Mesa obtenerEntidad(Long id);

    void abrirCuenta(Long idMesa);

    void cerrarCuenta(Long idMesa);

    long contarConCuentaAbierta();
}