package com.app.ventas.Dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarroResponse {
    private Long carroId;
    private Long usuarioId;
    private LocalDateTime fechaCreacion;
    private Double subtotal;
    private Double descuento;
    private Double total;
    private String codigoCupon;
    private String estado;
    private List<DetalleResponse> detalles;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleResponse {
        private Long productoId;
        private int cantidad;
        private Double precioUnitario;
        private Double subtotal;
    }
}