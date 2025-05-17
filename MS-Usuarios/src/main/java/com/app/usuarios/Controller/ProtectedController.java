package com.app.usuarios.Controller;

import com.app.usuarios.Config.AuthClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProtectedController {
    private final AuthClientService authClientService;

    public ProtectedController(AuthClientService authClientService) {
        this.authClientService = authClientService;
    }

    @PostMapping("/protected-resource")
    public ResponseEntity<String> accessProtectedResource(@RequestHeader("Authorization") String token) {
        return ResponseEntity.status(HttpStatus.OK).body("succes");
    }
}
