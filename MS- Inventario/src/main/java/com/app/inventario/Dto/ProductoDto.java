package com.app.inventario.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {
    private Long id;
    private String codigoSku;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal costo;
    private Long categoriaId;
    private String categoriaNombre; // Para mostrar en respuestas
    private String serial;
    private String catalogo;
    private Boolean activo;
    private Integer stock;
    private LocalDateTime fechaCreacion;
    private Long proveedoresId;
}

