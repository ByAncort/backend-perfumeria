package com.app.ventas.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private Long id;
    private String codigoSku;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Double costo;
    private Integer stock;
    private Long categoriaId;
    private String catalogo;
    private String serial;
    private Long proveedorId;  // puede ser null
}