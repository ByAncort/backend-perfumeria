package com.app.auth.Controller;

import com.app.auth.Dto.*;
import com.app.auth.Exception.UserAlreadyExistsException;
import com.app.auth.Service.AuthService;
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
public class AuthController {
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


    @PostMapping(value = "login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("validate-token")
    public ResponseEntity<?> validateToken(@RequestBody TokenValidationRequest request) {
        authService.validateToken(request.getToken());
        return ResponseEntity.ok(
                TokenValidResponse.builder()
                        .message("Token válido")
                        .build()
        );
    }


    @PostMapping(value = "register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.createUser(request);
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AuthResponse.builder().message(e.getMessage()).build());
        } catch (ServiceException  e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder().message(e.getMessage()).build());
        }
    }

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception e) {
        AuthResponse errorResponse = AuthResponse.builder()
                .message("Error interno del servidor: " + e.getMessage())
                .build();

        return ResponseEntity.internalServerError().body(errorResponse);
    }
}