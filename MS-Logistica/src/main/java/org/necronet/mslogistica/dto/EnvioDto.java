package org.necronet.mslogistica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvioDto {
    private Long id;
    private Long pagoId;
    private Long usuarioId;
    private Long carritoId;
    private String codigoSeguimiento;
    private String estado;
    private String direccionEnvio;
    private String ciudad;
    private String provincia;
    private String codigoPostal;
    private String pais;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaEntrega;
    private String metodoEnvio;
    private String notas;
}