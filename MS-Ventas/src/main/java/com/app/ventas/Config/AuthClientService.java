package com.app.ventas.Config;

import com.app.usuarios.Config.Dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthClientService {
    private final RestTemplate restTemplate;
    @Value("${auth.url.jwtMicro}")
    private String AUTH_SERVICE_URL;

    public AuthClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validateToken(String token) {
        try {
            // Crea el cuerpo de la solicitud
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("token", token);

            // Realiza la petición
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    AUTH_SERVICE_URL,
                    requestBody,
                    TokenResponse.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (HttpClientErrorException e) {
            // Maneja errores 4xx
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BadCredentialsException("Token inválido");
            }
            throw new RuntimeException("Error de comunicación con el servicio de autenticación");
        }
    }
}
