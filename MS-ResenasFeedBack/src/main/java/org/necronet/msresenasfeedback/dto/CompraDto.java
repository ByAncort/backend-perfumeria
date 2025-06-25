package org.necronet.msresenasfeedback.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompraDto {
    private Long id;
    private Double monto;
    private LocalDateTime fecha;
    private String descripcion;
}