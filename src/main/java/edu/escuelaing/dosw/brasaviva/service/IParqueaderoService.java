package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.EntradaVehiculoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.DisponibilidadParqueaderoDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.RegistroVehiculoResponseDTO;

import java.util.List;

public interface IParqueaderoService {

    RegistroVehiculoResponseDTO registrarEntrada(EntradaVehiculoRequestDTO dto);

    RegistroVehiculoResponseDTO registrarSalida(String placa);

    List<RegistroVehiculoResponseDTO> obtenerActivos();

    DisponibilidadParqueaderoDTO obtenerDisponibilidad();
}