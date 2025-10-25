package com.example.backendplantshop.service.impl;

import com.example.backendplantshop.convert.UserConvert;
import com.example.backendplantshop.dto.request.users.UserDtoRequest;
import com.example.backendplantshop.dto.response.user.LoginDtoResponse;
import com.example.backendplantshop.dto.response.user.UserDtoResponse;
import com.example.backendplantshop.entity.Users;
import com.example.backendplantshop.enums.ErrorCode;
import com.example.backendplantshop.exception.AppException;
import com.example.backendplantshop.mapper.*;
import com.example.backendplantshop.service.intf.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final AuthServiceImpl authService;
    private final CartDetailMapper cartDetailMapper;
    private final CartMapper cartMapper;
    private final UserTokenMapper userTokenMapper;


    private String clean(String input) {
        return (input != null && !input.trim().isEmpty()) ? input : null;
    }


    public UserDtoResponse findById(int id) {
        // Chỉ ADMIN hoặc user login
        int currentUserId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();
        if (!authService.isAdmin(role) && currentUserId != id) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        var users = userMapper.findById(id);
        if (users == null) {
            throw  new AppException(ErrorCode.USER_NOT_EXISTS);
        }
        return UserConvert.convertUsersToUserDtoResponse(users);
    }


    public List<UserDtoResponse> findAllUsers() {
        // Chỉ ADMIN mới được xem danh sách
        if (!authService.isAdmin(authService.getCurrentRole())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        var users = UserConvert.convertListUserToListUserDtoResponse(userMapper.findAll());
        if (users == null) {
            throw  new AppException(ErrorCode.LIST_NOT_FOUND);
        }
        return users;
    }


    public LoginDtoResponse update(int id, UserDtoRequest userDtoRequest) {
        // Chỉ ADMIN hoặc chính chủ mới được sửa
        int currentUserId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();
        if (!authService.isAdmin(role) && currentUserId != id) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        Users existingUser = userMapper.findById(id);
        if (existingUser == null) {
            throw  new AppException(ErrorCode.USER_NOT_EXISTS);
        }

        userDtoRequest.setEmail(clean(userDtoRequest.getEmail()));
        userDtoRequest.setPhone_number(clean(userDtoRequest.getPhone_number()));
        userDtoRequest.setRole(clean(userDtoRequest.getRole()));
        userDtoRequest.setAddress(clean(userDtoRequest.getAddress()));



        // Kiểm tra trùng lặp - chỉ kiểm tra khi có giá trị
        Users duplicateEmail = userMapper.findByEmail(userDtoRequest.getEmail());
        if (duplicateEmail != null && duplicateEmail.getUser_id() != id) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Users duplicatePhone = userMapper.findByPhoneNumber(userDtoRequest.getPhone_number());
        if (duplicatePhone != null && duplicatePhone.getUser_id() != id) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        Users duplicateUsername = userMapper.findByUsername(userDtoRequest.getUsername());
        if (duplicateUsername != null && duplicateUsername.getUser_id() != id) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // phát hiện đổi role
        boolean roleChanged = userDtoRequest.getRole() != null && !userDtoRequest.getRole().equals(existingUser.getRole());
        if (roleChanged) {
            if (!authService.isAdmin(role)) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
            // ADMIN đổi role thì update bình thường
        }
        userMapper.update(UserConvert.toUpdatedUser(id, userDtoRequest, existingUser));

        // Nếu role thay đổi: không cấp token mới, yêu cầu client đăng nhập lại
        if (roleChanged) {
            throw new AppException(ErrorCode.AUTHENTICATION_ERROR);
        }


        return null; // không đổi role thì không trả token
    }


    public void delete(int id) {
        // Chỉ ADMIN được xoá
        if (!authService.isAdmin(authService.getCurrentRole())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        if(userMapper.findById(id) == null){
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
        // 1. Xóa cart_details trước
    cartDetailMapper.deleteByUserId(id);
    
    // 2. Xóa cart
    cartMapper.deleteByUserId(id);

    userTokenMapper.revokeTokensByUser(id);
        userMapper.delete(id);
    }

    // Lấy thông tin user với logic bảo mật thông minh (không tự đọc token, dùng SecurityContext)
    public UserDtoResponse getUser(String authHeader, Integer id) {
        int currentUserId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();
        log.info(">>> Authorization header: {}", authHeader);

        int targetUserId = (id != null) ? id : currentUserId;

        if (id != null && !authService.isAdmin(role) && currentUserId != targetUserId) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        return findById(targetUserId);
    }


    // Lấy thông tin user với logic bảo mật thông minh
    // public UserDtoResponse getUser(String authHeader, Integer id) {
    //     String token = (authHeader != null && authHeader.startsWith("Bearer "))
    //             ? authHeader.substring(7)
    //             : null;

    //     if (token == null || !jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
    //         throw new AppException(ErrorCode.AUTHENTICATION_ERROR);
    //     }

    //     String role = jwtUtil.extractRole(token);
    //     if (role == null) {
    //         throw new AppException(ErrorCode.AUTHENTICATION_ERROR);
    //     }

    //     int currentUserId = jwtUtil.extractUserId(token);

    //     // 1. Nếu không truyền ID -> lấy thông tin user hiện tại
    //     // 2. Nếu truyền ID:
    //     //    - ADMIN: có thể xem thông tin của bất kỳ user nào
    //     //    - USER: chỉ có thể xem thông tin của chính mình
    //     int targetUserId = (id != null) ? id : currentUserId;

    //     //nếu ko có quyền admin và ko phải người dùng hiện tại
    //     if (id != null && !"ADMIN".equals(role) && currentUserId != targetUserId) {
    //         throw new AppException(ErrorCode.ACCESS_DENIED);
    //     }
        
    //     return findById(targetUserId);
    // }
    @Override
    public void restoreUser(int id) {
        if (!authService.isAdmin(authService.getCurrentRole())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        if(userMapper.findByIdDeleted(id) == null){
            throw new AppException(ErrorCode.NOT_DELETE);
        }
        cartMapper.restoreByUserId(id);
        cartDetailMapper.restoreByUserId(id);
        userMapper.restoreUser(id);
    }


}
