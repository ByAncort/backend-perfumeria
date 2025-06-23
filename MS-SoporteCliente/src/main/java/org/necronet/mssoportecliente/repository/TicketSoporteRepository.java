package org.necronet.mssoportecliente.repository;

import org.necronet.mssoportecliente.model.TicketSoporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketSoporteRepository extends JpaRepository<TicketSoporte, Long> {
    List<TicketSoporte> findByClienteId(Long clienteId);
    List<TicketSoporte> findByEstado(String estado);
    List<TicketSoporte> findByTipo(String tipo);
    List<TicketSoporte> findByPrioridad(String prioridad);
}