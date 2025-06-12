package com.app.usuarios.Controller;

import com.app.usuarios.Dto.ServiceResult;
import com.app.usuarios.Dto.UserDto;
import com.app.usuarios.Dto.UserResponseDto;
import com.app.usuarios.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Gestión de Usuarios", description = "API para la administración de usuarios del sistema")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Obtener todos los usuarios",
            description = "Endpoint para recuperar todos los usuarios registrados en el sistema",
            operationId = "getAllUsers"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponseDto[].class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        ServiceResult<List<UserResponseDto>> result = userService.getAllUsers();
        return result.hasErrors()
                ? ResponseEntity.status(500).body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }

    @Operation(
            summary = "Actualizar un usuario",
            description = "Endpoint para modificar los datos de un usuario existente",
            operationId = "updateUser"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "ID del usuario a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Datos actualizados del usuario", required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.class)))
            @RequestBody UserDto userDto) {
        ServiceResult<UserResponseDto> result = userService.updateUser(id, userDto);
        return result.hasErrors()
                ? ResponseEntity.badRequest().body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }

    @Operation(
            summary = "Eliminar un usuario",
            description = "Endpoint para eliminar un usuario del sistema",
            operationId = "deleteUser"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario eliminado exitosamente",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID del usuario a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        ServiceResult<String> result = userService.deleteUser(id);
        return result.hasErrors()
                ? ResponseEntity.status(404).body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }
}
