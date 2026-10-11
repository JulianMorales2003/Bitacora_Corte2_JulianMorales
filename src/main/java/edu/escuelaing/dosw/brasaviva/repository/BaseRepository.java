package edu.escuelaing.dosw.brasaviva.repository;

import java.util.List;
import java.util.Optional;

public interface BaseRepository<T, ID> {

    <S extends T> S save(S entidad);

    Optional<T> findById(ID id);

    List<T> findAll();

    void delete(T entidad);
}
