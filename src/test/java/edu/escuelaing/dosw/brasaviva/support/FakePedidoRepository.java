package edu.escuelaing.dosw.brasaviva.support;

import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.repository.PedidoRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class FakePedidoRepository extends FakeJpaRepository<Pedido, Long> implements PedidoRepository {

    public FakePedidoRepository() {
        super(Pedido::getId, Pedido::setId);
    }

    @Override
    public List<Pedido> findByIdMesaOrderByTimestampAsc(Long idMesa) {
        return datos.values().stream()
                .filter(p -> p.getIdMesa().equals(idMesa))
                .sorted(Comparator.comparing(Pedido::getTimestamp))
                .toList();
    }

    @Override
    public List<Pedido> findByEstado(EstadoPedido estado) {
        return datos.values().stream().filter(p -> p.getEstado() == estado).toList();
    }

    @Override
    public List<Pedido> findByEstadoInOrderByTimestampAsc(Collection<EstadoPedido> estados) {
        return datos.values().stream()
                .filter(p -> estados.contains(p.getEstado()))
                .sorted(Comparator.comparing(Pedido::getTimestamp))
                .toList();
    }

    @Override
    public List<Pedido> findByIdMesaAndTimestampGreaterThanEqual(Long idMesa, LocalDateTime desde) {
        return datos.values().stream()
                .filter(p -> p.getIdMesa().equals(idMesa) && !p.getTimestamp().isBefore(desde))
                .toList();
    }
}
