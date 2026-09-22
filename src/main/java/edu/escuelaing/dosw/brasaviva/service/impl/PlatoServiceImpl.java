package edu.escuelaing.dosw.brasaviva.service.impl;

import edu.escuelaing.dosw.brasaviva.dto.request.DisponibilidadRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.request.PlatoRequestDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.MenuItemResponseDTO;
import edu.escuelaing.dosw.brasaviva.dto.response.PlatoResponseDTO;
import edu.escuelaing.dosw.brasaviva.exception.PlatoNoEncontradoException;
import edu.escuelaing.dosw.brasaviva.exception.PlatoYaExisteException;
import edu.escuelaing.dosw.brasaviva.mapper.in.PlatoMapperIn;
import edu.escuelaing.dosw.brasaviva.mapper.out.PlatoMapperOut;
import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import edu.escuelaing.dosw.brasaviva.service.IPlatoService;
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
public class PlatoServiceImpl implements IPlatoService {

    private final PlatoMapperIn mapperIn;
    private final PlatoMapperOut mapperOut;

    private final Map<Long, Plato> platos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public List<PlatoResponseDTO> obtenerTodos() {
        log.debug("Consultando la carta completa");
        return platos.values().stream()
                .sorted(Comparator.comparing(Plato::getId))
                .map(mapperOut::toDTO)
                .toList();
    }

    @Override
    public List<PlatoResponseDTO> obtenerDisponibles() {
        return platos.values().stream()
                .filter(Plato::estaDisponible)
                .sorted(Comparator.comparing(Plato::getId))
                .map(mapperOut::toDTO)
                .toList();
    }

    @Override
    public PlatoResponseDTO obtenerPorId(Long id) {
        return mapperOut.toDTO(buscarOLanzar(id));
    }

    @Override
    public PlatoResponseDTO crear(PlatoRequestDTO dto) {
        if (nombreExiste(dto.nombre(), null)) {
            throw new PlatoYaExisteException("Ya existe un plato: " + dto.nombre());
        }
        Plato plato = mapperIn.toDomain(dto);
        plato.setId(secuencia.incrementAndGet());
        plato.setDisponible(true);
        platos.put(plato.getId(), plato);
        log.info("Plato creado: id={}, nombre={}", plato.getId(), plato.getNombre());
        return mapperOut.toDTO(plato);
    }

    @Override
    public PlatoResponseDTO actualizar(Long id, PlatoRequestDTO dto) {
        Plato plato = buscarOLanzar(id);
        if (nombreExiste(dto.nombre(), id)) {
            throw new PlatoYaExisteException("Ya existe otro plato llamado: " + dto.nombre());
        }
        plato.setNombre(dto.nombre());
        plato.setPrecio(dto.precio());
        plato.setCategoria(dto.categoria());
        plato.setDescripcion(dto.descripcion());
        plato.setTiempoPreparacionMin(dto.tiempoPreparacionMin());
        log.info("Plato actualizado: id={}", id);
        return mapperOut.toDTO(plato);
    }

    @Override
    public PlatoResponseDTO cambiarDisponibilidad(Long id, DisponibilidadRequestDTO dto) {
        Plato plato = buscarOLanzar(id);
        plato.cambiarDisponibilidad(dto.disponible());
        log.info("Plato id={} marcado como {}", id, dto.disponible() ? "DISPONIBLE" : "AGOTADO");
        return mapperOut.toDTO(plato);
    }

    @Override
    public void eliminar(Long id) {
        buscarOLanzar(id);
        platos.remove(id);
        log.info("Plato eliminado de la carta: id={}", id);
    }

    @Override
    public List<MenuItemResponseDTO> obtenerMenu(String categoria) {
        return platos.values().stream()
                .filter(Plato::estaDisponible)
                .filter(p -> categoria == null || p.getCategoria().equalsIgnoreCase(categoria))
                .sorted(Comparator.comparing(Plato::getCategoria).thenComparing(Plato::getNombre))
                .map(mapperOut::toMenuDTO)
                .toList();
    }

    @Override
    public MenuItemResponseDTO obtenerItemMenu(Long id) {
        return Optional.ofNullable(platos.get(id))
                .filter(Plato::estaDisponible)
                .map(mapperOut::toMenuDTO)
                .orElseThrow(() -> new PlatoNoEncontradoException("Plato no disponible en el menu: " + id));
    }

    @Override
    public Plato obtenerEntidad(Long id) {
        return buscarOLanzar(id);
    }

    private boolean nombreExiste(String nombre, Long idExcluido) {
        return platos.values().stream()
                .filter(p -> !p.getId().equals(idExcluido))
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));
    }

    private Plato buscarOLanzar(Long id) {
        return Optional.ofNullable(platos.get(id))
                .orElseThrow(() -> new PlatoNoEncontradoException("Plato no encontrado: " + id));
    }
}