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
import org.springframework.transaction.annotation.Transactional;
import edu.escuelaing.dosw.brasaviva.repository.MesaRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MesaServiceImpl implements IMesaService {

    private final MesaRepository mesaRepository;
    private final MesaMapperIn mapperIn;
    private final MesaMapperOut mapperOut;


    @Override
    public List<MesaResponseDTO> obtenerTodas(String estado) {
        return mesaRepository.findAll().stream()
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
        if (mesaRepository.existsByNumero(dto.numero())) {
            throw new MesaYaExisteException("Ya existe la mesa numero " + dto.numero());
        }
        Mesa mesa = mapperIn.toDomain(dto);
        mesa.setEstado(EstadoMesa.DISPONIBLE);
        mesa.setCuentaAbierta(false);
        mesa = mesaRepository.save(mesa);
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
        mesaRepository.save(mesa);
        log.info("Cuenta abierta en mesa {}", mesa.getNumero());
    }

    @Override
    public void cerrarCuenta(Long idMesa) {
        Mesa mesa = buscarOLanzar(idMesa);
        mesa.cerrarCuenta();
        mesaRepository.save(mesa);
        log.info("Mesa {} liberada", mesa.getNumero());
    }

    @Override
    public long contarConCuentaAbierta() {
        return mesaRepository.countByCuentaAbiertaTrue();
    }

    private Mesa buscarOLanzar(Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new MesaNoEncontradaException("Mesa no encontrada: " + id));
    }
}