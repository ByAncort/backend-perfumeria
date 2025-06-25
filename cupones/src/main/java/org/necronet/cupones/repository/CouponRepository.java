package org.necronet.cupones.repository;

import org.necronet.cupones.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    Optional<Coupon> findByCodeIgnoreCase(String code);
    List<Coupon> findByActiveTrue();
    List<Coupon> findByValidToBefore(LocalDateTime date);
    @Query("SELECT c FROM Coupon c WHERE c.active = true AND c.validFrom <= :currentDate AND c.validTo >= :currentDate")
    List<Coupon> findValidCoupons(@Param("currentDate") LocalDateTime currentDate);
    List<Coupon> findByDiscountType(String discountType);
    boolean existsByCode(String code);
    List<Coupon> findByDescriptionContainingIgnoreCase(String description);
    List<Coupon> findByDiscountValueGreaterThan(BigDecimal value);
    @Query("SELECT c FROM Coupon c WHERE c.validFrom BETWEEN :startDate AND :endDate OR c.validTo BETWEEN :startDate AND :endDate")
    List<Coupon> findCouponsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}