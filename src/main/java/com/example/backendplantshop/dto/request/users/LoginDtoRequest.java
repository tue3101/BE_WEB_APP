package com.example.backendplantshop.dto.request.users;

import com.example.backendplantshop.service.intf.ValidEmailOrPhone;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ValidEmailOrPhone(message = "phải có email hoặc số diện thoại")

public class LoginDtoRequest {
    private String email;
    private String phone_number;
    @NotBlank(message = "mật khẩu không được bỏ trống")
    private String password;

}
