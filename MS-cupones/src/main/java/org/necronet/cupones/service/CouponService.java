package org.necronet.cupones.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.dto.ServiceResult;
import org.necronet.cupones.dto.CouponDto;
import org.necronet.cupones.model.Coupon;
import org.necronet.cupones.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public ServiceResult<CouponDto> createCoupon(CouponDto couponDto) {
        List<String> errors = validateCoupon(couponDto);
        if (!errors.isEmpty()) {
            return new ServiceResult<>(errors);
        }

        try {
            Coupon coupon = Coupon.builder()
                    .code(couponDto.getCode())
                    .description(couponDto.getDescription())
                    .discountValue(couponDto.getDiscountValue())
                    .discountType(couponDto.getDiscountType())
                    .validFrom(couponDto.getValidFrom())
                    .validTo(couponDto.getValidTo())
                    .active(couponDto.isActive())
                    .build();

            Coupon savedCoupon = couponRepository.save(coupon);
            return new ServiceResult<>(convertToDto(savedCoupon));
        } catch (Exception e) {
            log.error("Error al crear cupón", e);
            errors.add("Error al crear el cupón: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<CouponDto> getCoupon(Long couponId) {
        List<String> errors = new ArrayList<>();
        try {
            return couponRepository.findById(couponId)
                    .map(coupon -> new ServiceResult<>(convertToDto(coupon)))
                    .orElseGet(() -> {
                        errors.add("Cupón no encontrado");
                        return new ServiceResult<>(errors);
                    });
        } catch (Exception e) {
            log.error("Error al obtener cupón ID: {}", couponId, e);
            errors.add("Error al obtener el cupón");
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<CouponDto> updateCoupon(Long couponId, CouponDto couponDto) {
        List<String> errors = validateCoupon(couponDto);
        if (!errors.isEmpty()) {
            return new ServiceResult<>(errors);
        }

        try {
            return couponRepository.findById(couponId)
                    .map(existingCoupon -> {
                        Coupon updatedCoupon = Coupon.builder()
                                .id(existingCoupon.getId())
                                .code(couponDto.getCode())
                                .description(couponDto.getDescription())
                                .discountValue(couponDto.getDiscountValue())
                                .discountType(couponDto.getDiscountType())
                                .validFrom(couponDto.getValidFrom())
                                .validTo(couponDto.getValidTo())
                                .active(couponDto.isActive())
                                .build();

                        Coupon savedCoupon = couponRepository.save(updatedCoupon);
                        return new ServiceResult<>(convertToDto(savedCoupon));
                    })
                    .orElseGet(() -> {
                        errors.add("Cupón no encontrado");
                        return new ServiceResult<>(errors);
                    });
        } catch (Exception e) {
            log.error("Error al actualizar cupón ID: {}", couponId, e);
            errors.add("Error al actualizar el cupón: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<Boolean> deleteCoupon(Long couponId) {
        List<String> errors = new ArrayList<>();
        try {
            if (couponRepository.existsById(couponId)) {
                couponRepository.deleteById(couponId);
                return new ServiceResult<>(true);
            }
            errors.add("Cupón no encontrado");
            return new ServiceResult<>(errors);
        } catch (Exception e) {
            log.error("Error al eliminar cupón ID: {}", couponId, e);
            errors.add("Error al eliminar el cupón");
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<CouponDto> validateCoupon(String code) {
        List<String> errors = new ArrayList<>();
        try {
            return couponRepository.findByCode(code)
                    .filter(coupon -> coupon.isActive() &&
                            LocalDateTime.now().isAfter(coupon.getValidFrom()) &&
                            LocalDateTime.now().isBefore(coupon.getValidTo()))
                    .map(coupon -> new ServiceResult<>(convertToDto(coupon)))
                    .orElseGet(() -> {
                        errors.add("Cupón no válido o expirado");
                        return new ServiceResult<>(errors);
                    });
        } catch (Exception e) {
            log.error("Error al validar cupón código: {}", code, e);
            errors.add("Error al validar el cupón");
            return new ServiceResult<>(errors);
        }
    }

    private CouponDto convertToDto(Coupon coupon) {
        return CouponDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountValue(coupon.getDiscountValue())
                .discountType(coupon.getDiscountType())
                .validFrom(coupon.getValidFrom())
                .validTo(coupon.getValidTo())
                .active(coupon.isActive())
                .build();
    }

    private List<String> validateCoupon(CouponDto couponDto) {
        List<String> errors = new ArrayList<>();

        if (couponDto.getCode() == null || couponDto.getCode().trim().isEmpty()) {
            errors.add("El código del cupón es requerido");
        }

        if (couponDto.getDiscountValue() == null || couponDto.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("El valor de descuento debe ser mayor que cero");
        }

        if (couponDto.getDiscountType() == null ||
                (!"PERCENTAGE".equals(couponDto.getDiscountType()) && !"FIXED".equals(couponDto.getDiscountType()))) {
            errors.add("Tipo de descuento inválido. Debe ser 'PERCENTAGE' o 'FIXED'");
        }

        if (couponDto.getValidFrom() == null || couponDto.getValidTo() == null ||
                couponDto.getValidFrom().isAfter(couponDto.getValidTo())) {
            errors.add("Las fechas de validez son inválidas");
        }

        return errors;
    }
}