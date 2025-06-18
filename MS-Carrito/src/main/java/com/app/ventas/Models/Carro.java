package com.app.ventas.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "carritos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false)
    @Builder.Default
    private String estado = "ACTIVO"; // Posibles valores: ACTIVO, VACIO, COMPLETADO, ABANDONADO

    @OneToMany(mappedBy = "carro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCarro> detalles;

    @Version
    private Long version; // Para control de concurrencia

    // Método helper para agregar detalles
    public void agregarDetalle(DetalleCarro detalle) {
        detalle.setCarro(this);
        this.detalles.add(detalle);
        calcularTotal();
    }

    // Método helper para calcular el total
    public void calcularTotal() {
        this.total = this.detalles.stream()
                .mapToDouble(DetalleCarro::getSubtotal)
                .sum();
    }
}