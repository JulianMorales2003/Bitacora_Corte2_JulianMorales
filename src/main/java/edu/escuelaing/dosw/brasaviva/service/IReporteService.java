package edu.escuelaing.dosw.brasaviva.service;

import edu.escuelaing.dosw.brasaviva.dto.response.IngresosResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoPopularDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ResumenDiaDTO;

import java.time.LocalDate;
import java.util.List;

public interface IReporteService {

    ResumenDiaDTO resumenDelDia();

    List<PlatoPopularDTO> platosPopulares(int top);

    IngresosResponseDTO ingresos(LocalDate desde, LocalDate hasta);
}