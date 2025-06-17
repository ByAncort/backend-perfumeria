package com.app.ventas.Dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class InventarioDto {
    private Long id;
    private ProductoDto producto;
    private SucursalDto sucursal;
    private Integer cantidad;
    private Integer stockMinimo;
    private LocalDateTime ultimaActualizacion;
    private TransferenciaDto transferencia;

    @Data
    public class ProductoDto {
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

    @Data
    public class SucursalDto {
        private Long id;
        private String nombre;
        private String direccion;
        private String ciudad;
        private String telefono;
        private LocalTime horarioApertura;
        private LocalTime horarioCierre;
        private Boolean activa;
    }

    @Data
    public class TransferenciaDto {
        private com.app.ventas.Dto.SucursalDto origen;
        private Integer cantidadTransferida;
    }

    @Data
    public class InventarioResponse {
        private Long id;
        private com.app.ventas.Dto.ProductoDto producto;
        private Integer cantidad;
        private Boolean success;
        private String message;

    }


}