package org.necronet.mslogistica.repository;

import org.necronet.mslogistica.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {
    Optional<Envio> findByPagoId(Long pagoId);
    Optional<Envio> findByCodigoSeguimiento(String codigoSeguimiento);
    List<Envio> findByUsuarioId(Long usuarioId);
}