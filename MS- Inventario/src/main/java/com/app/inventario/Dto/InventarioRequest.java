package com.app.inventario.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioRequest {
    private Long productoId;
    private Long sucursalId;
    private Integer cantidad;
    private Integer stockMinimo;
}