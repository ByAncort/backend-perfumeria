package com.app.usuarios.Dto;


import com.app.usuarios.Model.Role;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private Date issuedAt;
    private Date expiresAt;
    private String username;
    private Set<Role> roles;
    private String message;

}
