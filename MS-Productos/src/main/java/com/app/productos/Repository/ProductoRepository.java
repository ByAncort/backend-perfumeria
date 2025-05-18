package com.app.productos.Repository;

import com.app.productos.Models.Categoria;
import com.app.productos.Models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto,Long> {
    boolean existsByCodigoSku(String codigoSku);
    List<Producto> findByCategoria(Categoria categoria);
}
