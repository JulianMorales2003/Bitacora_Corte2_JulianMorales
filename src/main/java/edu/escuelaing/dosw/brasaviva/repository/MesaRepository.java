package edu.escuelaing.dosw.brasaviva.repository;

import edu.escuelaing.dosw.brasaviva.model.domain.Mesa;

public interface MesaRepository extends BaseRepository<Mesa, Long> {
    boolean existsByNumero(Integer numero);

    long countByCuentaAbiertaTrue();
}
