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
public class VentaRequestDto {
    private String clienteIdentificacion;
    private List<ItemVentaDto> items;
}

