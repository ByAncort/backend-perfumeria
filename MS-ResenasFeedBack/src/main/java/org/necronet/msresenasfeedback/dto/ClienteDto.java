package org.necronet.msresenasfeedback.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ClienteDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String segmento;
    private List<ContactoDto> contactos;
    private List<CompraDto> compras;
}