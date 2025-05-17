package com.app.inventario.Repository;

import com.app.inventario.Models.Inventario;
import com.app.inventario.Models.Producto;
import com.app.inventario.Models.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    // Buscar registro por producto y sucursal
    Optional<Inventario> findByProductoAndSucursal(Producto producto, Sucursal sucursal);

    // Stock bajo (usando stock mínimo del producto)
    @Query("SELECT i FROM Inventario i WHERE i.cantidad <= i.stockMinimo")
    List<Inventario> findProductosBajoStock();

    // Inventario por sucursal
    List<Inventario> findBySucursal(Sucursal sucursal);

    List<Inventario> findByProducto(Producto producto);
}