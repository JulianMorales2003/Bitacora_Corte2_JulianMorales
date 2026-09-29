package edu.escuelaing.dosw.brasaviva.repository;

import edu.escuelaing.dosw.brasaviva.model.domain.Pedido;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByIdMesaOrderByTimestampAsc(Long idMesa);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByEstadoInOrderByTimestampAsc(Collection<EstadoPedido> estados);

    List<Pedido> findByIdMesaAndTimestampGreaterThanEqual(Long idMesa, LocalDateTime desde);

}
