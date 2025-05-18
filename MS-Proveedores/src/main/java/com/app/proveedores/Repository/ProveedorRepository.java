package com.app.proveedores.Repository;

import com.app.proveedores.Models.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    boolean existsByRut(String normalizarRut);

    Optional<Proveedor> findByRut(String rut);

    List<Proveedor> findByActivoTrue();

}
