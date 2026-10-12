package edu.escuelaing.dosw.brasaviva.persistence.adapter;

import edu.escuelaing.dosw.brasaviva.model.domain.Usuario;
import edu.escuelaing.dosw.brasaviva.persistence.entity.UsuarioEntity;
import edu.escuelaing.dosw.brasaviva.persistence.jpa.UsuarioJpaRepository;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.UsuarioEntityMapper;
import edu.escuelaing.dosw.brasaviva.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Implementa el contrato de dominio {@link UsuarioRepository} sobre Spring Data JPA. */
@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpa;
    private final UsuarioEntityMapper mapper;

    @Override
    public <S extends Usuario> S save(S usuario) {
        UsuarioEntity guardado = jpa.save(mapper.toEntity(usuario));
        usuario.setId(guardado.getId());
        return usuario;
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Usuario> findAll() {
        return jpa.findAll(Sort.by("id")).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void delete(Usuario usuario) {
        jpa.deleteById(usuario.getId());
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public long count() {
        return jpa.count();
    }
}
