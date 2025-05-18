package com.app.ventas.Dto;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVentaDto {
    private Long productoId;
    private Integer cantidad;
}

