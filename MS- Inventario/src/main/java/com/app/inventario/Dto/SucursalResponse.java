package com.app.inventario.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
