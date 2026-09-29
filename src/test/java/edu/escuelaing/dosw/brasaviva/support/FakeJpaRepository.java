package edu.escuelaing.dosw.brasaviva.support;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Repositorio en memoria para pruebas unitarias: sustituye al repositorio JPA real
 * sin necesitar una base de datos, y asigna el id automaticamente como lo hace la BD.
 */
public abstract class FakeJpaRepository<T, ID extends Long> implements JpaRepository<T, ID> {

    protected final Map<Long, T> datos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);
    private final Function<T, Long> getId;
    private final java.util.function.BiConsumer<T, Long> setId;

    protected FakeJpaRepository(Function<T, Long> getId, java.util.function.BiConsumer<T, Long> setId) {
        this.getId = getId;
        this.setId = setId;
    }

    protected List<T> valoresOrdenados() {
        return datos.values().stream()
                .sorted(Comparator.comparing(getId::apply))
                .collect(Collectors.toList());
    }

    @Override
    public <S extends T> S save(S entidad) {
        Long id = getId.apply(entidad);
        if (id == null) {
            id = secuencia.incrementAndGet();
            setId.accept(entidad, id);
        }
        datos.put(id, entidad);
        return entidad;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<T> findAll() {
        return valoresOrdenados();
    }

    @Override
    public List<T> findAll(Sort sort) {
        return valoresOrdenados();
    }

    @Override
    public boolean existsById(ID id) {
        return datos.containsKey(id);
    }

    @Override
    public long count() {
        return datos.size();
    }

    @Override
    public void deleteById(ID id) {
        datos.remove(id);
    }

    @Override
    public void delete(T entidad) {
        datos.remove(getId.apply(entidad));
    }

    // --- Miembros de JpaRepository que estas pruebas no necesitan ---
    @Override public <S extends T> List<S> saveAll(Iterable<S> entidades) { throw notImplemented(); }
    @Override public List<T> findAllById(Iterable<ID> ids) { throw notImplemented(); }
    @Override public void deleteAllById(Iterable<? extends ID> ids) { throw notImplemented(); }
    @Override public void deleteAll(Iterable<? extends T> entidades) { throw notImplemented(); }
    @Override public void deleteAll() { datos.clear(); }
    @Override public void flush() { }
    @Override public <S extends T> S saveAndFlush(S entidad) { return save(entidad); }
    @Override public <S extends T> List<S> saveAllAndFlush(Iterable<S> entidades) { throw notImplemented(); }
    @Override public void deleteAllInBatch(Iterable<T> entidades) { throw notImplemented(); }
    @Override public void deleteAllByIdInBatch(Iterable<ID> ids) { throw notImplemented(); }
    @Override public void deleteAllInBatch() { datos.clear(); }
    @Override public T getOne(ID id) { return datos.get(id); }
    @Override public T getById(ID id) { return datos.get(id); }
    @Override public T getReferenceById(ID id) { return datos.get(id); }
    @Override public <S extends T> Optional<S> findOne(Example<S> example) { throw notImplemented(); }
    @Override public <S extends T> List<S> findAll(Example<S> example) { throw notImplemented(); }
    @Override public <S extends T> List<S> findAll(Example<S> example, Sort sort) { throw notImplemented(); }
    @Override public <S extends T> org.springframework.data.domain.Page<S> findAll(Example<S> example, Pageable pageable) { throw notImplemented(); }
    @Override public <S extends T> long count(Example<S> example) { throw notImplemented(); }
    @Override public <S extends T> boolean exists(Example<S> example) { throw notImplemented(); }
    @Override public <S extends T, R> R findBy(Example<S> example, Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { throw notImplemented(); }
    @Override public org.springframework.data.domain.Page<T> findAll(Pageable pageable) { throw notImplemented(); }

    private static UnsupportedOperationException notImplemented() {
        return new UnsupportedOperationException("No usado en las pruebas");
    }
}
