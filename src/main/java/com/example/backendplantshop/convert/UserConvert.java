package com.example.backendplantshop.convert;

import com.example.backendplantshop.dto.request.users.RegisterDtoRequest;
import com.example.backendplantshop.dto.request.users.UserDtoRequest;
import com.example.backendplantshop.dto.response.user.UserDtoResponse;
import com.example.backendplantshop.entity.Users;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

public class UserConvert {

    public static Users convertResigterDtoRequestToUsers(RegisterDtoRequest dto, PasswordEncoder passwordEncoder) {
        return Users.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .username(dto.getUsername())
                .phone_number(dto.getPhone_number())
                // luôn mặc định USER khi đăng ký, bỏ qua input role nếu có
                .role("USER")
                .is_deleted(false)
                .build();
    }

    public static UserDtoResponse convertUsersToUserDtoResponse(Users users) {
        return UserDtoResponse.builder()
                .user_id(users.getUser_id())
                .email(users.getEmail())
                .username(users.getUsername())
                .phone_number(users.getPhone_number())
                .address(users.getAddress())
                .role(users.getRole())
                .is_deleted(users.getIs_deleted())
                .build();
    }

    public static List<UserDtoResponse> convertListUserToListUserDtoResponse(List<Users> userList) {
        return userList.stream()
                .map(UserConvert::convertUsersToUserDtoResponse)
                .toList();
    }

    public static Users toUpdatedUser(int id, UserDtoRequest request, Users existingUser) {
        return Users.builder()
                .user_id(id)
                .email(request.getEmail() != null ? request.getEmail() : existingUser.getEmail())
                .username(request.getUsername() != null ? request.getUsername() : existingUser.getUsername())
                .phone_number(request.getPhone_number() != null ? request.getPhone_number() : existingUser.getPhone_number())
                .address(request.getAddress() != null ? request.getAddress() : existingUser.getAddress())
                // chỉ cập nhật role khi request cung cấp giá trị (đã chuẩn hóa rỗng->null)
                .role(request.getRole() != null ? request.getRole() : existingUser.getRole())
                .updated_at(LocalDateTime.now())
                .created_at(existingUser.getCreated_at())
                .is_deleted(existingUser.getIs_deleted())
                .build();
    }

}
