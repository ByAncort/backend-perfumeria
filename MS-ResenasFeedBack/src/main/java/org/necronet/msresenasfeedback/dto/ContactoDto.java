package org.necronet.msresenasfeedback.dto;

import lombok.Data;

@Data
public class ContactoDto {
    private Long id;
    private String tipo;     // por ejemplo: TELÉFONO, EMAIL, etc.
    private String valor;    // el dato del contacto
}