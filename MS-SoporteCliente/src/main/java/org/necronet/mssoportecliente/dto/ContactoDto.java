package org.necronet.mssoportecliente.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactoDto {
    private Long id;
    private String tipo;       // Ej: "TELÉFONO", "EMAIL"
    private String valor;      // Ej: "+56 9 1234 5678", "contacto@correo.cl"
}