package com.app.ventas.Dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarroRequest {
    private Long usuarioId;
    private String codigoCupon;
    private List<DetalleCarroRequest> detalles;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleCarroRequest {
        private Long inventarioId;
        private int cantidad;
    }
}