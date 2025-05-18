package com.app.ventas.Dto;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponseDto {
    private Long id;
    private LocalDateTime fecha;
    private String clienteIdentificacion;
    private BigDecimal total;
    private List<DetalleVentaDto> detalles;
}
