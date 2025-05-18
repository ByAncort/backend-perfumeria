package com.app.proveedores.Repostory;

import com.app.proveedores.Models.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor,Long> {
    boolean existsByRuc(String ruc);


}
