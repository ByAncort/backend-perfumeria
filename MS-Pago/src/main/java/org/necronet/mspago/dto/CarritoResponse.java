package org.necronet.mspago.dto;

import lombok.Data;
import java.util.List;

@Data
public class CarritoResponse {
    private Long id;
    private Long usuarioId;
    private String estado;
    private Double total;
    private List<ItemCarrito> items;

    @Data
    public static class ItemCarrito {
        private Long inventarioId;
        private Integer cantidad;
        private Double precioUnitario;
    }
}