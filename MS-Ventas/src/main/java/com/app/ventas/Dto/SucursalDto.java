package com.app.ventas.Dto;
import lombok.*;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SucursalDto {
    private Long sucursalId;
    private String nombreSucursal;
    private String direccionSucursal;
    private String ciudadSucursal;
    private String telefonoSucursal;
    private String horaApertura;
    private String horaCierre;
    private Boolean estaActiva;
}