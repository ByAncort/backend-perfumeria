package com.app.ventas.Dto;

import lombok.Data;
import java.util.List;

@Data
public class CarroRequest {

    private Long sucursalId;
    private Long clienteId;
    private List<DetalleVentaRequest> detalles;

    @Data
    public static class DetalleVentaRequest {
        private Long inventarioId; // id del registro de inventario contra el que se vende
        private Integer cantidad;
    }
}
