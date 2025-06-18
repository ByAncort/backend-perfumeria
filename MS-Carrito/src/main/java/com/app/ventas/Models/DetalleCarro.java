package com.app.ventas.Models;

import com.app.ventas.Dto.CarroRequest;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalle_carrito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCarro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Relaciones ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carro_id", nullable = false)
    private Carro carro;  // Cambiado de 'venta' a 'carro'

    // Referencia al producto en el microservicio de inventario
    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    // --- Datos de negocio ---
    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(nullable = false)
    private Double subtotal;

    // Método para calcular el subtotal
    public void calcularSubtotal() {
        if (this.precioUnitario != null && this.cantidad != null) {
            this.subtotal = this.precioUnitario * this.cantidad;
        }
    }

    // Método para actualizar desde DTO
    public void actualizarDesdeDto(CarroRequest.DetalleCarroRequest dto, Double precioUnitario) {
        this.cantidad = dto.getCantidad();
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }
}