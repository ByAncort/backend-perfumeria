package com.app.usuarios.Dto;

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

}
