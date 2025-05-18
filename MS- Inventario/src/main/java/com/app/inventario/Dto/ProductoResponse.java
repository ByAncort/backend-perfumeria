package com.app.inventario.Dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductoResponse {
    private Long id;
    private String codigoSku;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal costo;
    private Integer stock;
    private Long categoriaId;
    private String catalogo;
    private String serial;
    private Long proveedorId;
}
