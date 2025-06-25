package org.necronet.cupones.controller;

import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.necronet.cupones.dto.CouponDto;
import org.necronet.cupones.service.CouponService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ServiceResult<CouponDto> createCoupon(@RequestBody CouponDto couponDto) {
        return couponService.createCoupon(couponDto);
    }

    @GetMapping("/{couponId}")
    public ServiceResult<CouponDto> getCoupon(@PathVariable Long couponId) {
        return couponService.getCoupon(couponId);
    }

    @PutMapping("/{couponId}")
    public ServiceResult<CouponDto> updateCoupon(
            @PathVariable Long couponId,
            @RequestBody CouponDto couponDto) {
        return couponService.updateCoupon(couponId, couponDto);
    }

    @DeleteMapping("/{couponId}")
    public ServiceResult<Boolean> deleteCoupon(@PathVariable Long couponId) {
        return couponService.deleteCoupon(couponId);
    }

    @GetMapping("/validate/{code}")
    public ServiceResult<CouponDto> validateCoupon(@PathVariable String code) {
        return couponService.validateCoupon(code);
    }
}