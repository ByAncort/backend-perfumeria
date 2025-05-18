package com.app.ventas.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoInventarioDto {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
}