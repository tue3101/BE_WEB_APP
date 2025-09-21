package com.example.backendplantshop.dto.respones.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDtoResponse {
    private String accessToken;
    private String refreshToken;
}
