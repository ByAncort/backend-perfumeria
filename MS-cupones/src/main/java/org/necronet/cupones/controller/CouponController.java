package org.necronet.cupones.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

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

    @Operation(summary = "Crear un nuevo cupón",
            description = "Crea un cupón con los datos enviados en el cuerpo de la petición")
    @ApiResponse(responseCode = "200", description = "Cupón creado exitosamente",
            content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    @PostMapping
    public ServiceResult<CouponDto> createCoupon(
            @RequestBody
            @Parameter(description = "Datos del cupón a crear", required = true)
            CouponDto couponDto) {
        return couponService.createCoupon(couponDto);
    }

    @Operation(summary = "Obtener cupón por ID",
            description = "Devuelve un cupón existente por su ID")
    @ApiResponse(responseCode = "200", description = "Cupón encontrado",
            content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    @GetMapping("/{couponId}")
    public ServiceResult<CouponDto> getCoupon(
            @Parameter(description = "ID del cupón a obtener", required = true)
            @PathVariable Long couponId) {
        return couponService.getCoupon(couponId);
    }

    @Operation(summary = "Actualizar un cupón",
            description = "Actualiza los datos de un cupón existente por ID")
    @ApiResponse(responseCode = "200", description = "Cupón actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    @PutMapping("/{couponId}")
    public ServiceResult<CouponDto> updateCoupon(
            @Parameter(description = "ID del cupón a actualizar", required = true)
            @PathVariable Long couponId,
            @RequestBody
            @Parameter(description = "Datos actualizados del cupón", required = true)
            CouponDto couponDto) {
        return couponService.updateCoupon(couponId, couponDto);
    }

    @Operation(summary = "Eliminar un cupón",
            description = "Elimina un cupón por su ID")
    @ApiResponse(responseCode = "200", description = "Cupón eliminado exitosamente",
            content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    @DeleteMapping("/{couponId}")
    public ServiceResult<Boolean> deleteCoupon(
            @Parameter(description = "ID del cupón a eliminar", required = true)
            @PathVariable Long couponId) {
        return couponService.deleteCoupon(couponId);
    }

    @Operation(summary = "Validar un cupón por código",
            description = "Valida si un cupón está activo y es válido según su código")
    @ApiResponse(responseCode = "200", description = "Cupón válido",
            content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    @GetMapping("/validate/{code}")
    public ServiceResult<CouponDto> validateCoupon(
            @Parameter(description = "Código del cupón a validar", required = true)
            @PathVariable String code) {
        return couponService.validateCoupon(code);
    }
}
