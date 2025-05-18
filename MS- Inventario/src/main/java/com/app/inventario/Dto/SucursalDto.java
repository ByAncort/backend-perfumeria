package com.app.inventario.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SucursalDto {
    private Long id;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String telefono;
    private LocalTime horarioApertura;
    private LocalTime horarioCierre;
    private Boolean activa;
}
