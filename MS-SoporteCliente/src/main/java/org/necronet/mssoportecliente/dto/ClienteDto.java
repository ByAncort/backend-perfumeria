package org.necronet.mssoportecliente.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String segmento;

    @Builder.Default
    private List<ContactoDto> contactos = new ArrayList<>();

    @Builder.Default
    private List<CompraDto> compras = new ArrayList<>();
}