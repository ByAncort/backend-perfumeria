package com.app.proveedores.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorDto {
    private String nombre;
    private String rut;
    private String direccion;
    private String telefono;
    private String email;
    private boolean activo;
}
