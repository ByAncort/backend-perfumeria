package org.necronet.mssoportecliente.dto;

import lombok.Data;

@Data
public class TicketRequest {
    private Long clienteId;
    private String tipo;
    private String descripcion;
    private String prioridad;
    private String categoria;
}