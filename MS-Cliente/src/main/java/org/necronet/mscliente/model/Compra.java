package org.necronet.mscliente.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ventaId;
    private LocalDateTime fecha;
    private Double monto;
    private String producto;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}