package com.app.ventas.Dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarroResponse {
    private Long carroId;
    private Long usuarioId;
    private LocalDateTime fechaCreacion;
    private Double total;
    private String estado;
    private List<DetalleResponse> detalles;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleResponse {
        private Long productoId;
        private int cantidad;
        private Double precioUnitario;
        private Double subtotal;
    }
}