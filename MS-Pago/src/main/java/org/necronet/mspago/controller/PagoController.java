package org.necronet.mspago.controller;
import org.app.dto.ServiceResult;
import org.necronet.mspago.model.Pago;
import org.necronet.mspago.service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping("/procesar")
    public ResponseEntity<?> procesarPago(
            @RequestParam Long carritoId,
            @RequestParam String metodoPago) {

        ServiceResult<Pago> resultado = pagoService.procesarPago(carritoId, metodoPago);

        if (resultado.hasErrors()) {
            return ResponseEntity.badRequest().body(resultado.getErrors());
        }

        return ResponseEntity.created(URI.create("/api/pagos/" + resultado.getData().getId()))
                .body(resultado.getData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPago(@PathVariable Long id) {
        ServiceResult<Pago> resultado = pagoService.obtenerPagoPorId(id);

        if (resultado.hasErrors()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resultado.getData());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerPagosPorUsuario(
            @PathVariable Long usuarioId) {

        ServiceResult<List<Pago>> resultado = pagoService.obtenerPagosPorUsuario(usuarioId);

        if (resultado.hasErrors()) {
            return ResponseEntity.badRequest().body(resultado.getErrors());
        }

        return ResponseEntity.ok(resultado.getData());
    }

    @PostMapping("/{id}/reembolsar")
    public ResponseEntity<?> reembolsarPago(@PathVariable Long id) {
        ServiceResult<Pago> resultado = pagoService.reembolsarPago(id);

        if (resultado.hasErrors()) {
            return ResponseEntity.badRequest().body(resultado.getErrors());
        }

        return ResponseEntity.ok(resultado.getData());
    }
}