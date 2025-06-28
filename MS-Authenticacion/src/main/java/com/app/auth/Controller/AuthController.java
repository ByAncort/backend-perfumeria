package com.app.auth.Controller;

import com.app.auth.Dto.*;
import com.app.auth.Exception.UserAlreadyExistsException;
import com.app.auth.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API para manejo de autenticación y registro de usuarios")
public class AuthController {
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica a un usuario y devuelve un token JWT"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            )
    })
    @PostMapping("login")
    public ResponseEntity<EntityModel<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        EntityModel<AuthResponse> model = EntityModel.of(authResponse,
                linkTo(methodOn(AuthController.class).login(request)).withSelfRel(),
                linkTo(methodOn(AuthController.class).validateToken(new TokenValidationRequest(authResponse.getToken()))).withRel("validate-token")
        );

        return ResponseEntity.ok(model);
    }

    @Operation(
            summary = "Validar token",
            description = "Valida si un token JWT es válido",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token válido",
                    content = @Content(schema = @Schema(implementation = TokenValidResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado",
                    content = @Content(schema = @Schema(implementation = TokenValidResponse.class))
            )
    })
    @PostMapping("validate-token")
    public ResponseEntity<EntityModel<TokenValidResponse>> validateToken(@RequestBody TokenValidationRequest request) {
        authService.validateToken(request.getToken());

        TokenValidResponse response = TokenValidResponse.builder()
                .message("Token válido")
                .build();

        EntityModel<TokenValidResponse> model = EntityModel.of(response,
                linkTo(methodOn(AuthController.class).validateToken(request)).withSelfRel(),
                linkTo(methodOn(AuthController.class).login(null)).withRel("login")
        );

        return ResponseEntity.ok(model);
    }

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea una nueva cuenta de usuario"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El usuario ya existe",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            )
    })
    @PostMapping("register")
    public ResponseEntity<EntityModel<AuthResponse>> registerUser(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.createUser(request);

            EntityModel<AuthResponse> model = EntityModel.of(response,
                    linkTo(methodOn(AuthController.class).registerUser(request)).withSelfRel(),
                    linkTo(methodOn(AuthController.class).login(null)).withRel("login")
            );

            return ResponseEntity.ok(model);
        } catch (UserAlreadyExistsException e) {
            AuthResponse error = AuthResponse.builder().message(e.getMessage()).build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(EntityModel.of(error));
        } catch (ServiceException e) {
            AuthResponse error = AuthResponse.builder().message(e.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(EntityModel.of(error));
        }
    }

    @Operation(hidden = true)
    @ExceptionHandler({
            BadCredentialsException.class,
            AccessDeniedException.class,
            AuthenticationException.class
    })
    public ResponseEntity<EntityModel<AuthResponse>> handleAuthExceptions(RuntimeException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        if (e instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        }

        AuthResponse errorResponse = AuthResponse.builder()
                .message(e.getMessage())
                .build();

        EntityModel<AuthResponse> model = EntityModel.of(errorResponse,
                linkTo(methodOn(AuthController.class).login(null)).withRel("login")
        );

        return ResponseEntity.status(status).body(model);
    }

    @Operation(hidden = true)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<EntityModel<AuthResponse>> handleGenericException(Exception e) {
        AuthResponse errorResponse = AuthResponse.builder()
                .message("Error interno del servidor: " + e.getMessage())
                .build();

        EntityModel<AuthResponse> model = EntityModel.of(errorResponse,
                linkTo(methodOn(AuthController.class).login(null)).withRel("login"),
                linkTo(methodOn(AuthController.class).registerUser(null)).withRel("register")
        );

        return ResponseEntity.internalServerError().body(model);
    }
}
