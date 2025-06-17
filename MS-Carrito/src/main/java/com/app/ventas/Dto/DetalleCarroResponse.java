package com.app.ventas.Dto;
import lombok.*;

@Data
public class DetalleCarroResponse {
    private Long productoId;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}