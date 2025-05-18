package com.app.productos.Dto;

import lombok.Data;

@Data
public class ProveedorResponse {
    private Long id;
    private String ruc;
    private String nombre;
    private String direccion;
    private String contactoPrincipal;
    private String telefono;
    private String email;
    private boolean activo;
}

