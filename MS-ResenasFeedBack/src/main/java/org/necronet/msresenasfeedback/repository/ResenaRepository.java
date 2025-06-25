package org.necronet.msresenasfeedback.repository;

import org.necronet.msresenasfeedback.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByProductoId(Long productoId);

    List<Resena> findByClienteId(Long clienteId);

    boolean existsByClienteIdAndProductoId(Long clienteId, Long productoId);
}