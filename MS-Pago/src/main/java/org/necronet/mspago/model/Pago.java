package org.necronet.mspago.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long carritoId;
    private Long usuarioId;
    private Double monto;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    private EstadoPago estado;

    private String transaccionId;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaActualizacion;

    private String ultimosCuatroDigitos;
    private String nombreTitular;

    @PrePersist
    protected void onCreate() {
        fechaPago = LocalDateTime.now();
        transaccionId = UUID.randomUUID().toString();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}