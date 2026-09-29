package edu.escuelaing.dosw.brasaviva.service.impl;

import edu.escuelaing.dosw.brasaviva.config.BrasaVivaProperties;
import edu.escuelaing.dosw.brasaviva.dto.request.EntradaVehiculoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.DisponibilidadParqueaderoDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.RegistroVehiculoResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.ParqueaderoLlenoException;
import edu.escuelaing.dosw.brasaviva.exception.PlacaYaRegistradaException;
import edu.escuelaing.dosw.brasaviva.exception.VehiculoNoEncontradoException;
import edu.escuelaing.dosw.brasaviva.mapper.out.RegistroVehiculoMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.RegistroVehiculo;
import edu.escuelaing.dosw.brasaviva.service.IParqueaderoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.escuelaing.dosw.brasaviva.repository.RegistroVehiculoRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ParqueaderoServiceImpl implements IParqueaderoService {

    private final RegistroVehiculoRepository registroRepository;
    private final RegistroVehiculoMapperOut mapperOut;
    private final BrasaVivaProperties propiedades;
    private final Clock clock;


    @Override
    public RegistroVehiculoResponseDTO registrarEntrada(EntradaVehiculoRequestDTO dto) {
        String placa = dto.placa().toUpperCase();

        if (buscarActivo(placa).isPresent()) {
            throw new PlacaYaRegistradaException("La placa " + placa + " ya esta dentro del parqueadero");
        }
        if (contarActivos() >= propiedades.capacidadParqueadero()) {
            throw new ParqueaderoLlenoException("Parqueadero lleno: " + propiedades.capacidadParqueadero() + " cupos ocupados");
        }

        RegistroVehiculo registro = RegistroVehiculo.builder()
                .placa(placa)
                .entrada(LocalDateTime.now(clock))
                .build();
        registro = registroRepository.save(registro);
        log.info("Entrada de vehiculo {}", placa);
        return mapperOut.toDTO(registro);
    }

    @Override
    public RegistroVehiculoResponseDTO registrarSalida(String placa) {
        String placaNormalizada = placa.toUpperCase();
        RegistroVehiculo registro = buscarActivo(placaNormalizada)
                .orElseThrow(() -> new VehiculoNoEncontradoException(
                        "La placa " + placaNormalizada + " no tiene un ingreso activo"));
        registro.registrarSalida(LocalDateTime.now(clock));
        double cobro = registro.calcularCobro(propiedades.tarifaParqueaderoHora());
        registro = registroRepository.save(registro);
        log.info("Salida de vehiculo {}: cobro={}", placaNormalizada, cobro);
        return mapperOut.toDTO(registro);
    }

    @Override
    public List<RegistroVehiculoResponseDTO> obtenerActivos() {
        return registroRepository.findBySalidaIsNullOrderByEntradaAsc().stream()
                .map(mapperOut::toDTO)
                .toList();
    }

    @Override
    public DisponibilidadParqueaderoDTO obtenerDisponibilidad() {
        long ocupados = contarActivos();
        int capacidad = propiedades.capacidadParqueadero();
        return new DisponibilidadParqueaderoDTO(capacidad, ocupados, capacidad - ocupados);
    }

    private long contarActivos() {
        return registroRepository.countBySalidaIsNull();
    }

    private Optional<RegistroVehiculo> buscarActivo(String placa) {
        return registroRepository.findFirstByPlacaAndSalidaIsNull(placa);
    }
}