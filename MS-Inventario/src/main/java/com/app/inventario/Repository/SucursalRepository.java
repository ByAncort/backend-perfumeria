package com.app.inventario.Repository;

import com.app.inventario.Models.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    List<Sucursal> findByActivaTrue();
    List<Sucursal> findByCiudad(String ciudad);
}