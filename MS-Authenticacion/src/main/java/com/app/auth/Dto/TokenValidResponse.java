package com.app.auth.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenValidResponse {
    private String message;
}
