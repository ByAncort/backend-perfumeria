package org.necronet.mslogistica.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeguimientoResponse {
    private String codigoSeguimiento;
    private String estado;
    private LocalDateTime ultimaActualizacion;
    private String ubicacionActual;
    private LocalDateTime fechaEstimadaEntrega;
    private String mensaje;
}