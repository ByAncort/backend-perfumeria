package com.app.ventas.Dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {
    private Long id;
    private String code;
    private String description;
    private BigDecimal discountValue;

    @Builder.Default
    private String discountType = "PERCENTAGE";  // "PERCENTAGE" o "FIXED"

    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    @Builder.Default
    private boolean active = true;

    // Método mejorado para verificar validez
    public boolean isValid() {
        if (!active) return false;

        LocalDateTime now = LocalDateTime.now();
        return (validFrom == null || !now.isBefore(validFrom)) &&
                (validTo == null || !now.isAfter(validTo));
    }

    // Método mejorado para aplicar descuento
    public BigDecimal applyDiscount(BigDecimal amount) {
        if (amount == null || BigDecimal.ZERO.compareTo(amount) >= 0) {
            return BigDecimal.ZERO;
        }

        if (!isValid()) {
            return amount;
        }

        try {
            if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
                return amount.multiply(
                        BigDecimal.ONE.subtract(
                                discountValue.divide(BigDecimal.valueOf(100))
                        ));
            } else if ("FIXED".equalsIgnoreCase(discountType)) {
                return amount.subtract(discountValue).max(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            return amount;
        }
        return amount;
    }

    // Nuevo método: Calcula solo el monto del descuento
    public BigDecimal calculateDiscountAmount(BigDecimal amount) {
        if (!isValid() || amount == null) {
            return BigDecimal.ZERO;
        }

        try {
            if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
                return amount.multiply(discountValue.divide(BigDecimal.valueOf(100)));
            } else if ("FIXED".equalsIgnoreCase(discountType)) {
                return discountValue.min(amount); // No descuentar más que el monto total
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.ZERO;
    }

    // Nuevo método: Valida si el cupón puede aplicarse a un monto mínimo
    public boolean isValidForMinimumAmount(BigDecimal minimumAmount) {
        if (!isValid()) return false;

        if (minimumAmount == null || minimumAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }

        // Para descuentos fijos, verificar que el mínimo sea mayor que el descuento
        if ("FIXED".equalsIgnoreCase(discountType)) {
            return minimumAmount.compareTo(discountValue) >= 0;
        }

        return true;
    }
}