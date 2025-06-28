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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
    public ResponseEntity<EntityModel<ServiceResult<CouponDto>>> createCoupon(
            @RequestBody
            @Parameter(description = "Datos del cupón a crear", required = true)
            CouponDto couponDto) {
        ServiceResult<CouponDto> result = couponService.createCoupon(couponDto);

        EntityModel<ServiceResult<CouponDto>> resource = EntityModel.of(result);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).createCoupon(couponDto)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).getCoupon(result.getData().getId())).withRel("get-coupon"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).updateCoupon(result.getData().getId(), couponDto)).withRel("update-coupon"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).validateCoupon(result.getData().getCode())).withRel("validate-coupon"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Obtener cupón por ID",
            description = "Devuelve un cupón existente por su ID")
    @ApiResponse(responseCode = "200", description = "Cupón encontrado",
            content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    @GetMapping("/{couponId}")
    public ResponseEntity<EntityModel<ServiceResult<CouponDto>>> getCoupon(
            @Parameter(description = "ID del cupón a obtener", required = true)
            @PathVariable Long couponId) {
        ServiceResult<CouponDto> result = couponService.getCoupon(couponId);

        EntityModel<ServiceResult<CouponDto>> resource = EntityModel.of(result);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).getCoupon(couponId)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).updateCoupon(couponId, result.getData())).withRel("update-coupon"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).deleteCoupon(couponId)).withRel("delete-coupon"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).validateCoupon(result.getData().getCode())).withRel("validate-coupon"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Actualizar un cupón",
            description = "Actualiza los datos de un cupón existente por ID")
    @ApiResponse(responseCode = "200", description = "Cupón actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    @PutMapping("/{couponId}")
    public ResponseEntity<EntityModel<ServiceResult<CouponDto>>> updateCoupon(
            @Parameter(description = "ID del cupón a actualizar", required = true)
            @PathVariable Long couponId,
            @RequestBody
            @Parameter(description = "Datos actualizados del cupón", required = true)
            CouponDto couponDto) {
        ServiceResult<CouponDto> result = couponService.updateCoupon(couponId, couponDto);

        EntityModel<ServiceResult<CouponDto>> resource = EntityModel.of(result);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).updateCoupon(couponId, couponDto)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).getCoupon(couponId)).withRel("get-coupon"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).deleteCoupon(couponId)).withRel("delete-coupon"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).validateCoupon(result.getData().getCode())).withRel("validate-coupon"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Eliminar un cupón",
            description = "Elimina un cupón por su ID")
    @ApiResponse(responseCode = "200", description = "Cupón eliminado exitosamente",
            content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    @DeleteMapping("/{couponId}")
    public ResponseEntity<EntityModel<ServiceResult<Boolean>>> deleteCoupon(
            @Parameter(description = "ID del cupón a eliminar", required = true)
            @PathVariable Long couponId) {
        ServiceResult<Boolean> result = couponService.deleteCoupon(couponId);

        EntityModel<ServiceResult<Boolean>> resource = EntityModel.of(result);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).deleteCoupon(couponId)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).createCoupon(null)).withRel("create-coupon"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Validar un cupón por código",
            description = "Valida si un cupón está activo y es válido según su código")
    @ApiResponse(responseCode = "200", description = "Cupón válido",
            content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    @GetMapping("/validate/{code}")
    public ResponseEntity<EntityModel<ServiceResult<CouponDto>>> validateCoupon(
            @Parameter(description = "Código del cupón a validar", required = true)
            @PathVariable String code) {
        ServiceResult<CouponDto> result = couponService.validateCoupon(code);

        EntityModel<ServiceResult<CouponDto>> resource = EntityModel.of(result);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).validateCoupon(code)).withSelfRel());

        if (result.getData() != null) {
            resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).getCoupon(result.getData().getId())).withRel("get-coupon"));
            resource.add(WebMvcLinkBuilder.linkTo(methodOn(CouponController.class).updateCoupon(result.getData().getId(), result.getData())).withRel("update-coupon"));
        }

        return ResponseEntity.ok(resource);
    }
}