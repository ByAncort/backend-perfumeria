package com.app.inventario.Dto;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoBasicoDto {
    private Long id;
    private String codigoSku;
    private String nombre;
}

