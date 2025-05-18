package com.app.sucursales.Repository;

import com.app.sucursales.Models.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    List<Sucursal> findByActivaTrue();
    List<Sucursal> findByCiudad(String ciudad);
    boolean existsByNombre(String nombre);
}