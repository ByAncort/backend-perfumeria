package com.app.ventas.Dto;
import lombok.*;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaDto {
    private SucursalDto sucursalOrigen;
    private Integer cantidadTransferida;
}
