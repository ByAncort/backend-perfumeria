package org.necronet.mspago.repository;


import org.necronet.mspago.model.EstadoPago;
import org.necronet.mspago.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByUsuarioId(Long usuarioId);
    List<Pago> findByCarritoId(Long carritoId);
    List<Pago> findByEstado(EstadoPago estado);
}