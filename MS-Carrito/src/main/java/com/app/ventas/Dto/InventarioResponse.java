package com.app.ventas.Dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioResponse {
    private String mensaje;
    private Long inventarioId;
    private Integer cantidadActual;
    private String estado;
    private ProductoResponse producto;
    private SucursalResponse sucursal;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductoResponse {
        private Long id;
        private String nombre;
        private Double precio;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SucursalResponse {
        private Long id;
        private String nombre;
    }
}