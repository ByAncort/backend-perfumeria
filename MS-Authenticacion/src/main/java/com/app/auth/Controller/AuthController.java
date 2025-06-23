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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

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
    @PostMapping(value = "login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
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
    public ResponseEntity<?> validateToken(@RequestBody TokenValidationRequest request) {
        authService.validateToken(request.getToken());
        return ResponseEntity.ok(
                TokenValidResponse.builder()
                        .message("Token válido")
                        .build()
        );
    }



    @Operation(hidden = true)
    @ExceptionHandler({
            BadCredentialsException.class,
            AccessDeniedException.class,
            AuthenticationException.class
    })
    public ResponseEntity<AuthResponse> handleAuthExceptions(RuntimeException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        if (e instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        }

        AuthResponse errorResponse = AuthResponse.builder()
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @Operation(hidden = true)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception e) {
        AuthResponse errorResponse = AuthResponse.builder()
                .message("Error interno del servidor: " + e.getMessage())
                .build();

        return ResponseEntity.internalServerError().body(errorResponse);
    }
}