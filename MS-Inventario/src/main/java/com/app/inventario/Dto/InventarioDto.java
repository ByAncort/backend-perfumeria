package com.app.inventario.Dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDto {
    private Long productoId;
    private Long sucursalId;
    private Integer cantidad;
    private Integer stockMinimo;
}