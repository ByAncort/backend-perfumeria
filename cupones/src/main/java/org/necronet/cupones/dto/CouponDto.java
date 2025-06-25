package org.necronet.cupones.dto;

import lombok.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private String discountType;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private boolean active;
}