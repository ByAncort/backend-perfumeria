package com.app.usuarios.Controller;

import com.app.usuarios.Config.AuthClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Recursos Protegidos", description = "API para acceder a recursos que requieren autenticación")
@SecurityRequirement(name = "Bearer Authentication")
public class ProtectedController {
    private final AuthClientService authClientService;

    public ProtectedController(AuthClientService authClientService) {
        this.authClientService = authClientService;
    }

    @Operation(
            summary = "Acceder a recurso protegido",
            description = "Endpoint para acceder a un recurso que requiere autenticación mediante JWT",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "Token JWT de autenticación",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string", format = "jwt"),
                            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Acceso concedido al recurso protegido",
                    content = @Content(schema = @Schema(implementation = String.class, example = "success"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado - Token inválido o faltante",
                    content = @Content(schema = @Schema(implementation = String.class, example = "Unauthorized"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Prohibido - Token válido pero sin permisos suficientes",
                    content = @Content(schema = @Schema(implementation = String.class, example = "Forbidden"))
            )
    })
    @PostMapping("/protected-resource")
    public ResponseEntity<String> accessProtectedResource(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token) {

        if (!authClientService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        return ResponseEntity.status(HttpStatus.OK).body("success");
    }
}
