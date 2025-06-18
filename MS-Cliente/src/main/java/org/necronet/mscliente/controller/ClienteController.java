package org.necronet.mscliente.controller;

import org.necronet.mscliente.Service.ClienteService;
import org.necronet.mscliente.model.Cliente;
import org.necronet.mscliente.model.Compra;
import org.necronet.mscliente.model.Contacto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<Cliente> crearCliente(@RequestBody Cliente cliente) {
        Cliente nuevoCliente = clienteService.crearCliente(cliente);
        return ResponseEntity.created(URI.create("/api/clientes/" + nuevoCliente.getId()))
                .body(nuevoCliente);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerCliente(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerClientePorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(
            @PathVariable Long id, @RequestBody Cliente cliente) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarTodosLosClientes());
    }

    @GetMapping("/segmento/{segmento}")
    public ResponseEntity<List<Cliente>> listarClientesPorSegmento(
            @PathVariable String segmento) {
        return ResponseEntity.ok(clienteService.listarClientesPorSegmento(segmento));
    }

    @GetMapping("/{id}/compras")
    public ResponseEntity<List<Compra>> obtenerHistorialCompras(
            @PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerHistorialCompras(id));
    }

    @PostMapping("/{id}/contactos")
    public ResponseEntity<Cliente> agregarContacto(
            @PathVariable Long id, @RequestBody Contacto contacto) {
        return ResponseEntity.ok(clienteService.agregarContacto(id, contacto));
    }

    @PatchMapping("/{id}/segmento")
    public ResponseEntity<Cliente> actualizarSegmento(
            @PathVariable Long id, @RequestBody String segmento) {
        return ResponseEntity.ok(clienteService.actualizarSegmento(id, segmento));
    }
}