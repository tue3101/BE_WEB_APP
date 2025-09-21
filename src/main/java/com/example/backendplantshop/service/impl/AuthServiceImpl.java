package com.example.backendplantshop.service.impl;

import com.example.backendplantshop.convert.UserConvert;
import com.example.backendplantshop.dto.request.users.ChangePasswordDtoRequest;
import com.example.backendplantshop.dto.request.users.LoginDtoRequest;
import com.example.backendplantshop.dto.request.users.RegisterDtoRequest;
import com.example.backendplantshop.dto.respones.user.LoginDtoResponse;
import com.example.backendplantshop.entity.Users;
import com.example.backendplantshop.enums.ErrorCode;
import com.example.backendplantshop.exception.AppException;
import com.example.backendplantshop.mapper.UserMapper;
import com.example.backendplantshop.security.JwtUtil;
import com.example.backendplantshop.service.intf.AuthenticationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthenticationService {
    @Autowired
    private final UserMapper userMapper;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final JwtUtil jwtUtil;

    private String clean(String input) {
        return (input != null && !input.trim().isEmpty()) ? input : null;
    }

    public void register(RegisterDtoRequest registerDtoRequest) {
        if (registerDtoRequest == null) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        // Làm sạch dữ liệu: nếu email hoặc phone_number là chuỗi rỗng hoặc chỉ chứa khoảng trắng thì gán null
        registerDtoRequest.setEmail(clean(registerDtoRequest.getEmail()));
        registerDtoRequest.setPhone_number(clean(registerDtoRequest.getPhone_number()));


        // Kiểm tra trùng lặp - chỉ kiểm tra khi có giá trị
        if (userMapper.findByEmail(registerDtoRequest.getEmail()) != null) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userMapper.findByPhoneNumber(registerDtoRequest.getPhone_number()) != null) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }


        if (userMapper.findByUsername(registerDtoRequest.getUsername()) != null) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        Users users = UserConvert.convertResigterDtoRequestToUsers(registerDtoRequest, passwordEncoder);
        userMapper.insert(users);
    }


    public LoginDtoResponse login(LoginDtoRequest loginDtoRequest) {
        if (loginDtoRequest == null) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD);
        }


        // Tìm user theo email hoặc phone
        Users users = null;
        if (loginDtoRequest.getEmail() != null && !loginDtoRequest.getEmail().trim().isEmpty()) {
            users = userMapper.findByEmail(loginDtoRequest.getEmail());
        } else if (loginDtoRequest.getPhone_number() != null && !loginDtoRequest.getPhone_number().trim().isEmpty()) {
            users = userMapper.findByPhoneNumber(loginDtoRequest.getPhone_number());
        }

        if (users == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }

        // Kiểm tra password
        if (!passwordEncoder.matches(loginDtoRequest.getPassword(), users.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Cả access và refresh đều không dùng được → cấp cặp token mới
        String accessToken = jwtUtil.generateAccessToken(users.getUser_id(), users.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(users.getUser_id());

        // Trả về accessToken + refreshToken
        return LoginDtoResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public LoginDtoResponse refresh(String authHeader) {
        // Bóc token từ header
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7)
                : null;

        if (token == null) {
            throw new AppException(ErrorCode.AUTHENTICATION_ERROR);
        }



        // xác thực refresh token: chỉ kiểm tra chữ ký + hạn + type
        if (!jwtUtil.validateToken(token) || !jwtUtil.isRefreshToken(token)) {
            throw new AppException(ErrorCode.AUTHENTICATION_ERROR);
        }

        int id = jwtUtil.extractUserId(token);
        // Query role từ DB
        String role = findRoleByUserId(id);
        // Sinh access token mới
        String newAccessToken = jwtUtil.generateAccessToken(id, role);

        // Trả về cả access và refresh token
        return LoginDtoResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token) // giữ refresh token cũ, hoặc có thể rotate tại đây
                .build();
    }
//    @Override
//    public void changePassword(ChangePasswordDtoRequest changePasswordDtoRequest, String authHeader) {
//        try{
//            // Lấy token từ Authorization header
//            String token = (authHeader != null && authHeader.startsWith("Bearer "))
//                    ? authHeader.substring(7)
//                    : null;
//
//            if (token == null || !jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
//                throw new AppException(ErrorCode.AUTHENTICATION_ERROR);
//            }
//
//            int currentUserId = jwtUtil.extractUserId(token);
//            Users user = userMapper.findById(currentUserId);
//            if (user == null) {
//                throw new AppException(ErrorCode.USER_NOT_EXISTS);
//            }
//
//            // Kiểm tra mật khẩu cũ
//            if (!passwordEncoder.matches(changePasswordDtoRequest.getOldPassword(), user.getPassword())) {
//                log.error("Wrong old password for user {}", currentUserId);
//                throw new AppException(ErrorCode.INVALID_CREDENTIALS);
//            }
//
//            // Mã hóa mật khẩu mới
//            String encodedNewPassword = passwordEncoder.encode(changePasswordDtoRequest.getNewPassword());
//            userMapper.changePassword(currentUserId, encodedNewPassword);
//            log.info("Password changed successfully for user {}", currentUserId);
//        }catch (AppException e) {
//            log.error("AppException occurred while changing password for user {}: {}", e.getMessage());
//            throw e; // ném lại để ControllerAdvice xử lý trả response lỗi
//        } catch (Exception e) {
//            log.error("Unexpected error while changing password for user {}: {}", e.getMessage(), e);
//            throw new AppException(ErrorCode.AUTHENTICATION_ERROR);
//        }
//
//    }
    @Override
    public void changePassword(ChangePasswordDtoRequest changePasswordDtoRequest, String authHeader) {
        // Lấy token từ Authorization header
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7)
                : null;

        if (token == null || !jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
            throw new AppException(ErrorCode.AUTHENTICATION_ERROR);
        }
        // Lấy thông tin user từ token
        int currentUserId = jwtUtil.extractUserId(token);

        //check tồn tại user
        Users user =userMapper.findById(currentUserId);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }

        //check old pass
        if(!passwordEncoder.matches(changePasswordDtoRequest.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String enCodeNewPassword = passwordEncoder.encode(changePasswordDtoRequest.getNewPassword());
        int rows = userMapper.changePassword(currentUserId,enCodeNewPassword);
        if (rows == 0) {
            throw new AppException(ErrorCode.CHANGEPASSWORD_FAILED);
        }


    }


    @Override
    public String findRoleByUserId(int id) {
        Users users = userMapper.findById(id);
        if (users == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
        return users.getRole();
    }

}
