package edu.escuelaing.dosw.brasaviva.persistence.adapter;

import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.persistence.entity.PedidoEntity;
import edu.escuelaing.dosw.brasaviva.persistence.jpa.PedidoJpaRepository;
import edu.escuelaing.dosw.brasaviva.persistence.mapper.PedidoEntityMapper;
import edu.escuelaing.dosw.brasaviva.repository.PedidoRepository;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.ItemPedido;
import edu.escuelaing.dosw.brasaviva.persistence.entity.ItemPedidoEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * Implementa el contrato de dominio {@link PedidoRepository} sobre Spring Data JPA:
 * convierte dominio a entidad al guardar y entidad a dominio al leer.
 */
@Repository
@RequiredArgsConstructor
public class PedidoRepositoryAdapter implements PedidoRepository {

    private final PedidoJpaRepository jpa;
    private final PedidoEntityMapper mapper;

    @Override
    public <S extends Pedido> S save(S pedido) {
        PedidoEntity guardada = jpa.save(mapper.toEntity(pedido));
        pedido.setId(guardada.getId());
        List<ItemPedido> items = pedido.getItems();
        List<ItemPedidoEntity> guardados = guardada.getItems();
        if (items != null && guardados != null) {
            for (int i = 0; i < items.size() && i < guardados.size(); i++) {
                items.get(i).setId(guardados.get(i).getId());
            }
        }
        return pedido;
    }

    @Override
    public Optional<Pedido> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Pedido> findAll() {
        return aDominio(jpa.findAll(Sort.by("id")));
    }

    @Override
    public void delete(Pedido pedido) {
        jpa.deleteById(pedido.getId());
    }

    @Override
    public List<Pedido> findByIdMesaOrderByTimestampAsc(Long idMesa) {
        return aDominio(jpa.findByIdMesaOrderByTimestampAsc(idMesa));
    }

    @Override
    public List<Pedido> findByEstado(EstadoPedido estado) {
        return aDominio(jpa.findByEstado(estado));
    }

    @Override
    public List<Pedido> findByEstadoInOrderByTimestampAsc(Collection<EstadoPedido> estados) {
        return aDominio(jpa.findByEstadoInOrderByTimestampAsc(estados));
    }

    @Override
    public List<Pedido> findByIdMesaAndTimestampGreaterThanEqual(Long idMesa, LocalDateTime desde) {
        return aDominio(jpa.findByIdMesaAndTimestampGreaterThanEqual(idMesa, desde));
    }

    private List<Pedido> aDominio(List<PedidoEntity> entidades) {
        return entidades.stream().map(mapper::toDomain).toList();
    }
}
