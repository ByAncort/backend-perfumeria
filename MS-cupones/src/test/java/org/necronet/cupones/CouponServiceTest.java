package org.necronet.cupones;

import org.app.dto.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.necronet.cupones.dto.CouponDto;
import org.necronet.cupones.model.Coupon;
import org.necronet.cupones.repository.CouponRepository;
import org.necronet.cupones.service.CouponService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    private CouponDto validCouponDto;
    private Coupon validCoupon;
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime futureDate = now.plusDays(30);

    @BeforeEach
    void setUp() {
        validCouponDto = CouponDto.builder()
                .code("SUMMER20")
                .description("Summer discount")
                .discountValue(new BigDecimal("20.00"))
                .discountType("PERCENTAGE")
                .validFrom(now)
                .validTo(futureDate)
                .active(true)
                .build();

        validCoupon = Coupon.builder()
                .id(1L)
                .code("SUMMER20")
                .description("Summer discount")
                .discountValue(new BigDecimal("20.00"))
                .discountType("PERCENTAGE")
                .validFrom(now)
                .validTo(futureDate)
                .active(true)
                .build();
    }

    @Test
    void createCoupon_ShouldReturnCreatedCoupon_WhenValidInput() {
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(validCoupon);

        
        ServiceResult<CouponDto> result = couponService.createCoupon(validCouponDto);

        
        assertFalse(result.hasErrors());
        assertEquals("SUMMER20", result.getData().getCode());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void createCoupon_ShouldReturnErrors_WhenInvalidInput() {
        
        CouponDto invalidDto = CouponDto.builder()
                .code("")
                .discountValue(BigDecimal.ZERO)
                .discountType("INVALID")
                .build();

        
        ServiceResult<CouponDto> result = couponService.createCoupon(invalidDto);

        
        assertTrue(result.hasErrors());
        assertEquals(4, result.getErrors().size());
        verify(couponRepository, never()).save(any());
    }

    @Test
    void getCoupon_ShouldReturnCoupon_WhenExists() {
        
        when(couponRepository.findById(1L)).thenReturn(Optional.of(validCoupon));

        
        ServiceResult<CouponDto> result = couponService.getCoupon(1L);

        
        assertFalse(result.hasErrors());
        assertEquals(1L, result.getData().getId());
        verify(couponRepository, times(1)).findById(1L);
    }

    @Test
    void getCoupon_ShouldReturnError_WhenNotFound() {
        
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());

        
        ServiceResult<CouponDto> result = couponService.getCoupon(1L);

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("no encontrado"));
    }

    @Test
    void updateCoupon_ShouldUpdate_WhenValidInputAndExists() {
        
        CouponDto updateDto = validCouponDto.builder()
                .description("Updated description")
                .build();

        when(couponRepository.findById(1L)).thenReturn(Optional.of(validCoupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(validCoupon.builder()
                .description("Updated description")
                .build());

        
        ServiceResult<CouponDto> result = couponService.updateCoupon(1L, updateDto);

        
        assertFalse(result.hasErrors());
        assertEquals("Updated description", result.getData().getDescription());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void updateCoupon_ShouldReturnError_WhenNotFound() {
        
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());

        
        ServiceResult<CouponDto> result = couponService.updateCoupon(1L, validCouponDto);

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        verify(couponRepository, never()).save(any());
    }

    @Test
    void deleteCoupon_ShouldReturnTrue_WhenExists() {
        
        when(couponRepository.existsById(1L)).thenReturn(true);

        
        ServiceResult<Boolean> result = couponService.deleteCoupon(1L);

        
        assertFalse(result.hasErrors());
        assertTrue(result.getData());
        verify(couponRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCoupon_ShouldReturnError_WhenNotFound() {
        
        when(couponRepository.existsById(1L)).thenReturn(false);

        
        ServiceResult<Boolean> result = couponService.deleteCoupon(1L);

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        verify(couponRepository, never()).deleteById(any());
    }

    @Test
    void validateCoupon_ShouldReturnValidCoupon_WhenActiveAndInDateRange() {
        
        when(couponRepository.findByCode("SUMMER20")).thenReturn(Optional.of(validCoupon));

        
        ServiceResult<CouponDto> result = couponService.validateCoupon("SUMMER20");

        
        assertFalse(result.hasErrors());
        assertEquals("SUMMER20", result.getData().getCode());
    }

    @Test
    void validateCoupon_ShouldReturnError_WhenExpired() {
        
        Coupon expiredCoupon = validCoupon.builder()
                .validTo(now.minusDays(1))
                .build();

        when(couponRepository.findByCode("EXPIRED")).thenReturn(Optional.of(expiredCoupon));

        
        ServiceResult<CouponDto> result = couponService.validateCoupon("EXPIRED");

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("expirado"));
    }

    @Test
    void validateCoupon_ShouldReturnError_WhenInactive() {
        
        Coupon inactiveCoupon = validCoupon.builder()
                .active(false)
                .build();

        when(couponRepository.findByCode("INACTIVE")).thenReturn(Optional.of(inactiveCoupon));

        
        ServiceResult<CouponDto> result = couponService.validateCoupon("INACTIVE");

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("no válido"));
    }

    @Test
    void validateCoupon_ShouldReturnError_WhenNotFound() {
        
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        
        ServiceResult<CouponDto> result = couponService.validateCoupon("INVALID");

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("no válido"));
    }

    @Test
    void convertToDto_ShouldConvertCorrectly() {
        
        CouponDto result = couponService.convertToDto(validCoupon);

        
        assertEquals(1L, result.getId());
        assertEquals("SUMMER20", result.getCode());
        assertEquals("PERCENTAGE", result.getDiscountType());
        assertEquals(new BigDecimal("20.00"), result.getDiscountValue());
        assertTrue(result.isActive());
    }
}