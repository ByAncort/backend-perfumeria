package com.app.ventas.Dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CarroResponse {

    private Long ventaId;
    private Long sucursalId;
    private Long clienteId;
    private LocalDateTime fechaVenta;
    private Double total;
    private List<DetalleResponse> detalles;

    @Data
    @Builder
    public static class DetalleResponse {
        private Long productoId;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
    }
}
