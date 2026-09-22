package edu.escuelaing.dosw.brasaviva.controller;

import edu.escuelaing.dosw.brasaviva.dto.response.IngresosResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoPopularDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ResumenDiaDTO;
import edu.escuelaing.dosw.brasaviva.service.IReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes", description = "Inteligencia del negocio calculada con streams")
@Slf4j
@RequiredArgsConstructor
public class ReporteController {

    private final IReporteService reporteService;

    @Operation(summary = "Resumen del dia", description = "Total de pedidos, ingresos, platos mas pedidos y mesas con cuenta abierta")
    @ApiResponse(responseCode = "200", description = "Resumen calculado")
    @GetMapping("/resumen")
    public ResponseEntity<ResumenDiaDTO> resumen() {
        log.info("GET /api/v1/reportes/resumen");
        return ResponseEntity.ok(reporteService.resumenDelDia());
    }

    @Operation(summary = "Platos mas vendidos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking de platos"),
            @ApiResponse(responseCode = "400", description = "El top debe estar entre 1 y 50")
    })
    @GetMapping("/platos-populares")
    public ResponseEntity<List<PlatoPopularDTO>> platosPopulares(
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "El top minimo es 1")
            @Max(value = 50, message = "El top maximo es 50") int top) {
        log.info("GET /api/v1/reportes/platos-populares top={}", top);
        return ResponseEntity.ok(reporteService.platosPopulares(top));
    }

    @Operation(summary = "Ingresos por categoria en un rango de fechas", description = "Fechas en formato yyyy-MM-dd")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingresos calculados"),
            @ApiResponse(responseCode = "400", description = "Rango de fechas invalido")
    })
    @GetMapping("/ingresos")
    public ResponseEntity<IngresosResponseDTO> ingresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        log.info("GET /api/v1/reportes/ingresos {} a {}", desde, hasta);
        return ResponseEntity.ok(reporteService.ingresos(desde, hasta));
    }
}