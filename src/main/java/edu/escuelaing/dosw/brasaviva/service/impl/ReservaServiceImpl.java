package edu.escuelaing.dosw.brasaviva.service.impl;

import edu.escuelaing.dosw.brasaviva.dto.request.ReservaRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.ReservaResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.CapacidadMesaExcedidaException;
import edu.escuelaing.dosw.brasaviva.exception.EstadoInvalidoException;
import edu.escuelaing.dosw.brasaviva.exception.ReservaConflictoException;
import edu.escuelaing.dosw.brasaviva.exception.ReservaNoEncontradaException;
import edu.escuelaing.dosw.brasaviva.mapper.in.ReservaMapperIn;
import edu.escuelaing.dosw.brasaviva.mapper.out.ReservaMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoReserva;
import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import edu.escuelaing.dosw.brasaviva.model.domain.Reserva;
import edu.escuelaing.dosw.brasaviva.service.IMesaService;
import edu.escuelaing.dosw.brasaviva.service.IReservaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
public class ReservaServiceImpl implements IReservaService {

    private final IMesaService mesaService;
    private final ReservaMapperIn mapperIn;
    private final ReservaMapperOut mapperOut;

    private final Map<Long, Reserva> reservas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public ReservaResponseDTO crear(ReservaRequestDTO dto) {
        validarMesaYHorario(dto, null);
        Reserva reserva = mapperIn.toDomain(dto);
        reserva.setId(secuencia.incrementAndGet());
        reserva.setEstado(EstadoReserva.ACTIVA);
        reservas.put(reserva.getId(), reserva);
        log.info("Reserva {} creada: mesa id={}, {} para {} personas",
                reserva.getId(), dto.idMesa(), dto.fechaHora(), dto.comensales());
        return mapperOut.toDTO(reserva);
    }

    @Override
    public List<ReservaResponseDTO> obtenerTodas(String cliente) {
        return reservas.values().stream()
                .filter(r -> cliente == null || r.getCliente().toLowerCase().contains(cliente.toLowerCase()))
                .sorted(Comparator.comparing(Reserva::getFechaHora))
                .map(mapperOut::toDTO)
                .toList();
    }

    @Override
    public ReservaResponseDTO obtenerPorId(Long id) {
        return mapperOut.toDTO(buscarOLanzar(id));
    }

    @Override
    public ReservaResponseDTO actualizar(Long id, ReservaRequestDTO dto) {
        Reserva reserva = buscarOLanzar(id);
        validarActiva(reserva);
        validarMesaYHorario(dto, id);
        reserva.setIdMesa(dto.idMesa());
        reserva.setCliente(dto.cliente());
        reserva.setComensales(dto.comensales());
        reserva.reprogramar(dto.fechaHora());
        log.info("Reserva {} actualizada para {}", id, dto.fechaHora());
        return mapperOut.toDTO(reserva);
    }

    @Override
    public void cancelar(Long id) {
        Reserva reserva = buscarOLanzar(id);
        validarActiva(reserva);
        reserva.cancelar();
        log.info("Reserva {} cancelada", id);
    }

    private void validarMesaYHorario(ReservaRequestDTO dto, Long idExcluido) {
        Mesa mesa = mesaService.obtenerEntidad(dto.idMesa());
        if (dto.comensales() > mesa.getCapacidad()) {
            throw new CapacidadMesaExcedidaException("La mesa " + mesa.getNumero() + " es para "
                    + mesa.getCapacidad() + " personas y la reserva es para " + dto.comensales());
        }
        if (hayChoqueDeHorario(dto.idMesa(), dto.fechaHora(), idExcluido)) {
            throw new ReservaConflictoException("La mesa " + mesa.getNumero()
                    + " ya esta reservada cerca de " + dto.fechaHora());
        }
    }

    private boolean hayChoqueDeHorario(Long idMesa, LocalDateTime fechaHora, Long idExcluido) {
        return reservas.values().stream()
                .filter(r -> !r.getId().equals(idExcluido))
                .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
                .filter(r -> r.getIdMesa().equals(idMesa))
                .anyMatch(r -> r.seSolapaCon(fechaHora));
    }

    private void validarActiva(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new EstadoInvalidoException("La reserva " + reserva.getId() + " ya esta " + reserva.getEstado());
        }
    }

    private Reserva buscarOLanzar(Long id) {
        return Optional.ofNullable(reservas.get(id))
                .orElseThrow(() -> new ReservaNoEncontradaException("Reserva no encontrada: " + id));
    }
}