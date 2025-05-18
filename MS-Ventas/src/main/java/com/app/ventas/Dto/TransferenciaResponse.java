package com.app.ventas.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaResponse {
    private SucursalResponse origen;
    private Integer cantidadTransferida;
}

