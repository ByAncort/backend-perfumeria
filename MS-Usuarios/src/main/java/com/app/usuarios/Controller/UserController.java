package com.app.usuarios.Controller;

import com.app.usuarios.Dto.*;
import com.app.usuarios.Service.UserService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Gestión de Usuarios", description = "API para la administración de usuarios del sistema")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Obtener todos los usuarios", description = "Endpoint para recuperar todos los usuarios registrados en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        ServiceResult<List<UserResponseDto>> result = userService.getAllUsers();

        if (result.hasErrors()) {
            return ResponseEntity.status(500).body(result.getErrors());
        }

        List<EntityModel<UserResponseDto>> users = result.getData().stream()
                .map(user -> {
                    EntityModel<UserResponseDto> resource = EntityModel.of(user);
                    resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));
                    resource.add(linkTo(methodOn(UserController.class).updateUser(user.getId(), new UserDto())).withRel("update"));
                    resource.add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete"));
                    return resource;
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel();
        CollectionModel<EntityModel<UserResponseDto>> resources = CollectionModel.of(users, selfLink);

        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Actualizar un usuario", description = "Endpoint para modificar los datos de un usuario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "ID del usuario a actualizar", required = true, example = "1") @PathVariable Long id,
            @Parameter(description = "Datos actualizados del usuario", required = true) @RequestBody UserDto userDto) {

        ServiceResult<UserResponseDto> result = userService.updateUser(id, userDto);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        UserResponseDto user = result.getData();
        EntityModel<UserResponseDto> resource = EntityModel.of(user);
        resource.add(linkTo(methodOn(UserController.class).updateUser(id, userDto)).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Eliminar un usuario", description = "Endpoint para eliminar un usuario del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID del usuario a eliminar", required = true, example = "1") @PathVariable Long id) {

        ServiceResult<String> result = userService.deleteUser(id);

        if (result.hasErrors()) {
            return ResponseEntity.status(404).body(result.getErrors());
        }

        EntityModel<String> resource = EntityModel.of(result.getData());
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.ok(resource);
    }
}