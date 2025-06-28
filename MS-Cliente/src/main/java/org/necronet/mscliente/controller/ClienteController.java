package org.necronet.mscliente.controller;

import org.necronet.mscliente.Service.ClienteService;
import org.necronet.mscliente.model.Cliente;
import org.necronet.mscliente.model.Compra;
import org.necronet.mscliente.model.Contacto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes API", description = "Operaciones relacionadas con la gestión de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo cliente", description = "Registra un nuevo cliente en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<EntityModel<Cliente>> crearCliente(@RequestBody Cliente cliente) {
        Cliente nuevoCliente = clienteService.crearCliente(cliente);

        EntityModel<Cliente> resource = EntityModel.of(nuevoCliente);
        resource.add(linkTo(methodOn(ClienteController.class).crearCliente(cliente)).withSelfRel());
        resource.add(linkTo(methodOn(ClienteController.class).obtenerCliente(nuevoCliente.getId())).withRel("self"));
        resource.add(linkTo(methodOn(ClienteController.class).actualizarCliente(nuevoCliente.getId(), cliente)).withRel("update"));
        resource.add(linkTo(methodOn(ClienteController.class).eliminarCliente(nuevoCliente.getId())).withRel("delete"));
        resource.add(linkTo(methodOn(ClienteController.class).obtenerHistorialCompras(nuevoCliente.getId())).withRel("compras"));
        resource.add(linkTo(methodOn(ClienteController.class).agregarContacto(nuevoCliente.getId(), new Contacto())).withRel("add-contact"));
        resource.add(linkTo(methodOn(ClienteController.class).listarClientes()).withRel("all-clientes"));

        return ResponseEntity.created(URI.create("/api/clientes/" + nuevoCliente.getId()))
                .body(resource);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Recupera la información de un cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<EntityModel<Cliente>> obtenerCliente(
            @Parameter(description = "ID del cliente a buscar") @PathVariable Long id) {
        Cliente cliente = clienteService.obtenerClientePorId(id);

        EntityModel<Cliente> resource = EntityModel.of(cliente);
        resource.add(linkTo(methodOn(ClienteController.class).obtenerCliente(id)).withSelfRel());
        resource.add(linkTo(methodOn(ClienteController.class).actualizarCliente(id, cliente)).withRel("update"));
        resource.add(linkTo(methodOn(ClienteController.class).eliminarCliente(id)).withRel("delete"));
        resource.add(linkTo(methodOn(ClienteController.class).obtenerHistorialCompras(id)).withRel("compras"));
        resource.add(linkTo(methodOn(ClienteController.class).agregarContacto(id, new Contacto())).withRel("add-contact"));
        resource.add(linkTo(methodOn(ClienteController.class).listarClientes()).withRel("all-clientes"));
        resource.add(linkTo(methodOn(ClienteController.class)
                .listarClientesPorSegmento(cliente.getSegmento())).withRel("clientes-segmento"));

        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza la información de un cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<EntityModel<Cliente>> actualizarCliente(
            @Parameter(description = "ID del cliente a actualizar") @PathVariable Long id,
            @RequestBody Cliente cliente) {
        Cliente clienteActualizado = clienteService.actualizarCliente(id, cliente);

        EntityModel<Cliente> resource = EntityModel.of(clienteActualizado);
        resource.add(linkTo(methodOn(ClienteController.class).actualizarCliente(id, cliente)).withSelfRel());
        resource.add(linkTo(methodOn(ClienteController.class).obtenerCliente(id)).withRel("self"));
        resource.add(linkTo(methodOn(ClienteController.class).eliminarCliente(id)).withRel("delete"));
        resource.add(linkTo(methodOn(ClienteController.class).obtenerHistorialCompras(id)).withRel("compras"));
        resource.add(linkTo(methodOn(ClienteController.class).listarClientes()).withRel("all-clientes"));

        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> eliminarCliente(
            @Parameter(description = "ID del cliente a eliminar") @PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar todos los clientes", description = "Obtiene una lista de todos los clientes registrados")
    @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente")
    public ResponseEntity<CollectionModel<EntityModel<Cliente>>> listarClientes() {
        List<EntityModel<Cliente>> clientes = clienteService.listarTodosLosClientes().stream()
                .map(cliente -> {
                    EntityModel<Cliente> resource = EntityModel.of(cliente);
                    resource.add(linkTo(methodOn(ClienteController.class).obtenerCliente(cliente.getId())).withRel("self"));
                    resource.add(linkTo(methodOn(ClienteController.class).actualizarCliente(cliente.getId(), cliente)).withRel("update"));
                    resource.add(linkTo(methodOn(ClienteController.class).obtenerHistorialCompras(cliente.getId())).withRel("compras"));
                    return resource;
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(ClienteController.class).listarClientes()).withSelfRel();
        Link createLink = linkTo(methodOn(ClienteController.class).crearCliente(new Cliente())).withRel("create-cliente");

        CollectionModel<EntityModel<Cliente>> resources = CollectionModel.of(clientes, selfLink, createLink);

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/segmento/{segmento}")
    @Operation(summary = "Listar clientes por segmento", description = "Obtiene clientes filtrados por segmento de mercado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "No se encontraron clientes para el segmento especificado")
    })
    public ResponseEntity<CollectionModel<EntityModel<Cliente>>> listarClientesPorSegmento(
            @Parameter(description = "Segmento de mercado para filtrar") @PathVariable String segmento) {
        List<EntityModel<Cliente>> clientes = clienteService.listarClientesPorSegmento(segmento).stream()
                .map(cliente -> {
                    EntityModel<Cliente> resource = EntityModel.of(cliente);
                    resource.add(linkTo(methodOn(ClienteController.class).obtenerCliente(cliente.getId())).withRel("self"));
                    resource.add(linkTo(methodOn(ClienteController.class).listarClientesPorSegmento(segmento)).withRel("segmento"));
                    return resource;
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(ClienteController.class).listarClientesPorSegmento(segmento)).withSelfRel();
        Link allClientesLink = linkTo(methodOn(ClienteController.class).listarClientes()).withRel("all-clientes");

        CollectionModel<EntityModel<Cliente>> resources = CollectionModel.of(clientes, selfLink, allClientesLink);

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}/compras")
    @Operation(summary = "Obtener historial de compras", description = "Recupera el historial de compras de un cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de compras obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<CollectionModel<Compra>> obtenerHistorialCompras(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        List<Compra> compras = clienteService.obtenerHistorialCompras(id);

        // Si se quisiera implementar HATEOAS para las compras también:
        // Se podría crear un EntityModel/CollectionModel para Compra similar a lo hecho con Cliente

        return ResponseEntity.ok(CollectionModel.of(compras,
                linkTo(methodOn(ClienteController.class).obtenerHistorialCompras(id)).withSelfRel(),
                linkTo(methodOn(ClienteController.class).obtenerCliente(id)).withRel("cliente")));
    }

    @PostMapping("/{id}/contactos")
    @Operation(summary = "Agregar contacto a cliente", description = "Añade un nuevo contacto a un cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contacto agregado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de contacto inválidos")
    })
    public ResponseEntity<EntityModel<Cliente>> agregarContacto(
            @Parameter(description = "ID del cliente") @PathVariable Long id,
            @RequestBody Contacto contacto) {
        Cliente cliente = clienteService.agregarContacto(id, contacto);

        EntityModel<Cliente> resource = EntityModel.of(cliente);
        resource.add(linkTo(methodOn(ClienteController.class).agregarContacto(id, contacto)).withSelfRel());
        resource.add(linkTo(methodOn(ClienteController.class).obtenerCliente(id)).withRel("cliente"));

        return ResponseEntity.ok(resource);
    }

    @PatchMapping("/{id}/segmento")
    @Operation(summary = "Actualizar segmento de cliente", description = "Actualiza el segmento de mercado de un cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Segmento actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "400", description = "Segmento inválido")
    })
    public ResponseEntity<EntityModel<Cliente>> actualizarSegmento(
            @Parameter(description = "ID del cliente") @PathVariable Long id,
            @RequestBody String segmento) {
        Cliente cliente = clienteService.actualizarSegmento(id, segmento);

        EntityModel<Cliente> resource = EntityModel.of(cliente);
        resource.add(linkTo(methodOn(ClienteController.class).actualizarSegmento(id, segmento)).withSelfRel());
        resource.add(linkTo(methodOn(ClienteController.class).obtenerCliente(id)).withRel("cliente"));
        resource.add(linkTo(methodOn(ClienteController.class).listarClientesPorSegmento(segmento)).withRel("clientes-segmento"));

        return ResponseEntity.ok(resource);
    }
}