package com.app.ventas.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponse {
    private Long id;
    private ProductoResponse producto;
    private SucursalResponse sucursal;
    private Integer cantidad;
    private Integer stockMinimo;
    private LocalDateTime ultimaActualizacion;
    private TransferenciaResponse transferencia;  // Puede ser null
}





