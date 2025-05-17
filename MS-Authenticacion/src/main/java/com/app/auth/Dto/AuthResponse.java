package com.app.auth.Dto;

import com.app.auth.Models.Role;
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
