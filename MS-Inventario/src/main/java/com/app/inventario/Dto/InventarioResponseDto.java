package com.app.inventario.Dto;
import com.app.inventario.Models.Inventario;
import lombok.*;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponseDto {
    private Long id;
    private Long productoId;
    private Long sucursalId;
    private Integer cantidad;
    private Integer stockMinimo;
    private LocalDateTime ultimaActualizacion;
    public static InventarioResponseDto fromEntity(Inventario inventario) {
        return InventarioResponseDto.builder()
                .id(inventario.getId())
                .productoId(inventario.getProducto().getId())
                .sucursalId(inventario.getSucursal().getId())
                .cantidad(inventario.getCantidad())
                .stockMinimo(inventario.getStockMinimo())
                .ultimaActualizacion(inventario.getUltimaActualizacion())
                .build();
    }

}