package edu.escuelaing.dosw.brasaviva.repository;

import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    boolean existsByNumero(Integer numero);

    long countByCuentaAbiertaTrue();

}
