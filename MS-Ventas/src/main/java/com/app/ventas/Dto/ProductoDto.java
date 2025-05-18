package com.app.ventas.Dto;
import lombok.*;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {
    private Long productoId;
    private String skuCodigo;
    private String nombreProducto;
    private String descripcionProducto;
    private Double precioVenta;
    private Double costoProducto;
    private Integer stockActual;
    private Long categoriaProductoId;
    private String catalogoProducto;
    private String serialProducto;
    private Long proveedorId;  // puede ser null
}
