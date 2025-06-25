package org.necronet.mslogistica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDto {
    private Long id;
    private Long carritoId;
    private Long usuarioId;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private String transaccionId;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaActualizacion;
    private String ultimosCuatroDigitos;
    private String nombreTitular;
}