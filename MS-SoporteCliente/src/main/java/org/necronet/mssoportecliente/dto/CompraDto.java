package org.necronet.mssoportecliente.dto;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraDto {
    private Long id;
    private LocalDateTime fechaCompra;
    private BigDecimal montoTotal;
    private String metodoPago;
}