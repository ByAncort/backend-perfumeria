package com.app.ventas.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarroRequest {
    @NotNull(message = "El ID de usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "La lista de detalles no puede ser nula")
    private List<DetalleCarroRequest> detalles;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleCarroRequest {
        @NotNull(message = "El ID de inventario es obligatorio")
        private Long inventarioId;

        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        private int cantidad;
    }
}