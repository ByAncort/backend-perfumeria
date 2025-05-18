package com.app.inventario.Models;

import com.app.inventario.Dto.ProductoDto;
import com.app.inventario.Dto.SucursalDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Long id;

    @Column(name = "id_producto", nullable = false)
    private Long productoId;

    @Column(name = "id_sucursal", nullable = false)
    private Long sucursalId;

    @Column(nullable = false)
    @Builder.Default
    private Integer cantidad = 0;

    @Column(name = "stock_minimo")
    @Builder.Default
    private Integer stockMinimo = 1;

    @Column(name = "ultima_actualizacion")
    @Builder.Default
    private LocalDateTime ultimaActualizacion = LocalDateTime.now();

    @Transient
    private ProductoDto producto; // Datos del producto (no persistido)

    @Transient
    private SucursalDto sucursal; // Datos de la sucursal (no persistido)
}