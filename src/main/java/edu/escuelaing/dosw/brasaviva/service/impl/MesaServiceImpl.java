package edu.escuelaing.dosw.brasaviva.service.impl;

import edu.escuelaing.dosw.brasaviva.dto.request.MesaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.MesaResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.CuentaYaAbiertaException;
import edu.escuelaing.dosw.brasaviva.exception.MesaNoEncontradaException;
import edu.escuelaing.dosw.brasaviva.exception.MesaYaExisteException;
import edu.escuelaing.dosw.brasaviva.mapper.in.MesaMapperIn;
import edu.escuelaing.dosw.brasaviva.mapper.out.MesaMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoMesa;
import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import edu.escuelaing.dosw.brasaviva.service.IMesaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class MesaServiceImpl implements IMesaService {

    private final MesaMapperIn mapperIn;
    private final MesaMapperOut mapperOut;

    private final Map<Long, Mesa> mesas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public List<MesaResponseDTO> obtenerTodas(String estado) {
        return mesas.values().stream()
                .filter(m -> estado == null || m.getEstado().name().equalsIgnoreCase(estado))
                .sorted(Comparator.comparing(Mesa::getNumero))
                .map(mapperOut::toDTO)
                .toList();
    }

    @Override
    public MesaResponseDTO obtenerPorId(Long id) {
        return mapperOut.toDTO(buscarOLanzar(id));
    }

    @Override
    public MesaResponseDTO crear(MesaRequestDTO dto) {
        boolean numeroRepetido = mesas.values().stream()
                .anyMatch(m -> m.getNumero().equals(dto.numero()));
        if (numeroRepetido) {
            throw new MesaYaExisteException("Ya existe la mesa numero " + dto.numero());
        }
        Mesa mesa = mapperIn.toDomain(dto);
        mesa.setId(secuencia.incrementAndGet());
        mesa.setEstado(EstadoMesa.DISPONIBLE);
        mesa.setCuentaAbierta(false);
        mesas.put(mesa.getId(), mesa);
        log.info("Mesa creada: numero={}, capacidad={}", mesa.getNumero(), mesa.getCapacidad());
        return mapperOut.toDTO(mesa);
    }

    @Override
    public Mesa obtenerEntidad(Long id) {
        return buscarOLanzar(id);
    }

    @Override
    public void abrirCuenta(Long idMesa) {
        Mesa mesa = buscarOLanzar(idMesa);
        if (mesa.tieneCuentaAbierta()) {
            throw new CuentaYaAbiertaException("La mesa " + mesa.getNumero() + " ya tiene una cuenta abierta");
        }
        mesa.abrirCuenta();
        log.info("Cuenta abierta en mesa {}", mesa.getNumero());
    }

    @Override
    public void cerrarCuenta(Long idMesa) {
        Mesa mesa = buscarOLanzar(idMesa);
        mesa.cerrarCuenta();
        log.info("Mesa {} liberada", mesa.getNumero());
    }

    @Override
    public long contarConCuentaAbierta() {
        return mesas.values().stream().filter(Mesa::tieneCuentaAbierta).count();
    }

    private Mesa buscarOLanzar(Long id) {
        return Optional.ofNullable(mesas.get(id))
                .orElseThrow(() -> new MesaNoEncontradaException("Mesa no encontrada: " + id));
    }
}