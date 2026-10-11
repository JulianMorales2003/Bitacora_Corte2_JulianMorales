package edu.escuelaing.dosw.brasaviva.persistence.jpa;

import edu.escuelaing.dosw.brasaviva.persistence.entity.PedidoEntity;
import edu.escuelaing.dosw.brasaviva.model.domain.EstadoPedido;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data sobre la entidad PedidoEntity. */
public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findByIdMesaOrderByTimestampAsc(Long idMesa);

    List<PedidoEntity> findByEstado(EstadoPedido estado);

    List<PedidoEntity> findByEstadoInOrderByTimestampAsc(Collection<EstadoPedido> estados);

    List<PedidoEntity> findByIdMesaAndTimestampGreaterThanEqual(Long idMesa, LocalDateTime desde);
}
