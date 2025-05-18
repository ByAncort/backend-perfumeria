package com.app.productos.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ProductoDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal costo;
    private String serial;
    private String catalogo;
    private String codigoSku;
    private boolean activo;
    private String categoriaNombre;
    private List<Long> proveedorIds;
}
