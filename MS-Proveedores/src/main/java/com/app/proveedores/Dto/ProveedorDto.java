package com.app.proveedores.Dto;

import lombok.*;


@Data
@Builder
public class ProveedorDto {
    private Long id;
    private String ruc;
    private String nombre;
    private String direccion;
    private String contactoPrincipal;
    private String telefono;
    private String email;
    private boolean activo;
}
