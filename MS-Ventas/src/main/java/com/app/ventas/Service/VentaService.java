package com.app.ventas.Service;

import com.app.ventas.Dto.ProveedorResponse;
import com.app.ventas.shared.MicroserviceClient;
import com.app.ventas.shared.TokenContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final MicroserviceClient microserviceClient;

    public ProveedorResponse consultarProveedor(Long proveedorId) {
        String token = TokenContext.getToken();
        String url = "http://localhost:9012/api/ms-inventario/proveedor/" + proveedorId;
        ResponseEntity<ProveedorResponse> response = microserviceClient.enviarConToken(
                url,
                HttpMethod.GET,
                null,
                ProveedorResponse.class,
                token
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al obtener proveedor");
        }

        return response.getBody();
    }
}
