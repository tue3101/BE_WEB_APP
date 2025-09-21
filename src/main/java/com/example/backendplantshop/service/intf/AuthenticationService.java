package com.example.backendplantshop.service.intf;

import com.example.backendplantshop.dto.request.users.ChangePasswordDtoRequest;
import com.example.backendplantshop.dto.request.users.LoginDtoRequest;
import com.example.backendplantshop.dto.request.users.RegisterDtoRequest;
import com.example.backendplantshop.dto.respones.user.LoginDtoResponse;

public interface AuthenticationService {
    void register(RegisterDtoRequest registerDtoRequest);
    LoginDtoResponse login(LoginDtoRequest loginDtoRequest);
    String findRoleByUserId(int id);
    LoginDtoResponse refresh(String refreshToken);
    void changePassword(ChangePasswordDtoRequest changePasswordDtoRequest, String authHeader);
}
