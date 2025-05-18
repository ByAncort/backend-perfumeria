package com.app.ventas.Dto;
import lombok.*;

import java.util.List;

@Data
public class DetalleVentaResponse {
    private Long productoId;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}