package com.example.backendplantshop.controller;

import com.example.backendplantshop.dto.request.users.ChangePasswordDtoRequest;
import com.example.backendplantshop.dto.request.users.LoginDtoRequest;
import com.example.backendplantshop.dto.request.users.RegisterDtoRequest;
import com.example.backendplantshop.dto.respones.ApiResponse;
import com.example.backendplantshop.dto.respones.user.LoginDtoResponse;
import com.example.backendplantshop.enums.ErrorCode;
import com.example.backendplantshop.security.JwtUtil;
import com.example.backendplantshop.service.intf.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/register")
    ApiResponse<Void> registerUser(@Valid @RequestBody RegisterDtoRequest registerDtoRequest) {
        authenticationService.register(registerDtoRequest);
        return ApiResponse.<Void>builder()
                .statusCode(ErrorCode.REGISTER_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.REGISTER_SUCCESSFULL.getMessage())
                .build();
    }

    @PostMapping("/login")
    ApiResponse<LoginDtoResponse> loginUser(@Valid @RequestBody LoginDtoRequest loginDtoRequest) {
        return ApiResponse.<LoginDtoResponse>builder()
                .statusCode(ErrorCode.LOGIN_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.LOGIN_SUCCESSFULL.getMessage())
                .data(authenticationService.login(loginDtoRequest))
                .build();
    }

    @PutMapping("/change-password")
    ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordDtoRequest changePasswordDtoRequest,@RequestHeader("Authorization") String authHeader) {
       authenticationService.changePassword(changePasswordDtoRequest, authHeader);
        return ApiResponse.<Void>builder()
                .statusCode(ErrorCode.CHANGEPASSWORD_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.CHANGEPASSWORD_SUCCESSFULL.getMessage())
                .build();
    }

    // Endpoint làm mới access token bằng refresh token
    @PostMapping("/refresh")
    ApiResponse<LoginDtoResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        LoginDtoResponse response = authenticationService.refresh(authHeader);
        return ApiResponse.<LoginDtoResponse>builder()
                .statusCode(ErrorCode.LOGIN_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message("Làm mới access token thành công")
                .data(response)
                .build();
    }
}
