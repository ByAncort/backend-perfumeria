package com.app.inventario.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SucursalResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String telefono;
    private String horarioApertura;
    private String horarioCierre;
    private Boolean activa;
}
