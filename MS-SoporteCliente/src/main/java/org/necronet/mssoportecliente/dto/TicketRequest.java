package org.necronet.mssoportecliente.dto;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class TicketRequest {
    private Long clienteId;
    private String tipo;
    private String descripcion;
    private String prioridad;
    private String categoria;
}