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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParqueaderoServiceImpl implements IParqueaderoService {

    private final RegistroVehiculoMapperOut mapperOut;
    private final BrasaVivaProperties propiedades;
    private final Clock clock;

    private final Map<Long, RegistroVehiculo> registros = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

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
                .id(secuencia.incrementAndGet())
                .placa(placa)
                .entrada(LocalDateTime.now(clock))
                .build();
        registros.put(registro.getId(), registro);
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
        log.info("Salida de vehiculo {}: cobro={}", placaNormalizada, cobro);
        return mapperOut.toDTO(registro);
    }

    @Override
    public List<RegistroVehiculoResponseDTO> obtenerActivos() {
        return registros.values().stream()
                .filter(RegistroVehiculo::estaActivo)
                .sorted(Comparator.comparing(RegistroVehiculo::getEntrada))
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
        return registros.values().stream().filter(RegistroVehiculo::estaActivo).count();
    }

    private Optional<RegistroVehiculo> buscarActivo(String placa) {
        return registros.values().stream()
                .filter(RegistroVehiculo::estaActivo)
                .filter(r -> r.getPlaca().equals(placa))
                .findFirst();
    }
}