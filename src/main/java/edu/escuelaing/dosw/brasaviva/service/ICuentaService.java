package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.request.AbrirCuentaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PagoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.CuentaResponseDTO;

public interface ICuentaService {

    CuentaResponseDTO abrir(AbrirCuentaRequestDTO dto);

    CuentaResponseDTO obtenerPorId(Long id);

    CuentaResponseDTO obtenerAbiertaPorMesa(Long idMesa);

    CuentaResponseDTO registrarPago(Long idCuenta, PagoRequestDTO dto);
}