package com.app.inventario.Dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaResponse {
    private SucursalResponse origen;
    private Integer cantidadTransferida;
}
