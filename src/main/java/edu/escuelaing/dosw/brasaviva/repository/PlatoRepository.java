package edu.escuelaing.dosw.brasaviva.repository;

import edu.escuelaing.dosw.brasaviva.model.domain.Plato;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatoRepository extends JpaRepository<Plato, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Plato> findByDisponibleTrue();

}
