package com.app.inventario.Dto;

import lombok.*;

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
