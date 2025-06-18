package com.app.ventas.Dto;

import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioDto {
    private Long id;
    private ProductoDto producto;
    private SucursalDto sucursal;
    private Integer cantidad;
    private Integer stockMinimo;
    private LocalDateTime ultimaActualizacion;
    private TransferenciaDto transferencia;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductoDto {
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
        private Long proveedorId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SucursalDto {
        private Long id;
        private String nombre;
        private String direccion;
        private String ciudad;
        private String telefono;
        private LocalTime horarioApertura;
        private LocalTime horarioCierre;
        private Boolean activa;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransferenciaDto {
        private SucursalDto origen;
        private Integer cantidadTransferida;
    }
}