package com.zidio.dto;
import lombok.*;
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String role;
    private String username;
}
