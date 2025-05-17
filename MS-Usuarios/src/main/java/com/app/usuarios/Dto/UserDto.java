package com.app.usuarios.Dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserDto {
    private String username;
    private String email;
    private String password;
    private Set<String> roles; // Lista de nombres de roles a asignar
}
