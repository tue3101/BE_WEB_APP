package com.example.backendplantshop.service.impl;

import com.example.backendplantshop.convert.UserConvert;
import com.example.backendplantshop.dto.request.users.ChangePasswordDtoRequest;
import com.example.backendplantshop.dto.request.users.LoginDtoRequest;
import com.example.backendplantshop.dto.request.users.RegisterDtoRequest;
import com.example.backendplantshop.dto.respones.user.LoginDtoResponse;
import com.example.backendplantshop.entity.UserTokens;
import com.example.backendplantshop.entity.Users;
import com.example.backendplantshop.enums.ErrorCode;
import com.example.backendplantshop.exception.AppException;
import com.example.backendplantshop.mapper.UserMapper;
import com.example.backendplantshop.security.JwtUtil;
import com.example.backendplantshop.service.intf.AuthenticationService;
import com.example.backendplantshop.service.intf.UserTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthenticationService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserTokenService userTokenService;

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

        // Lưu token vào DB
        userTokenService.saveToken(UserTokens.builder()
                .user_id(users.getUser_id())
                .token(refreshToken) // chỉ lưu refreshToken trong DB
                .expires_at(LocalDateTime.now().plusDays(7)) // hạn refresh token
                .revoked(false)
                .build());
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

        // Validate refresh token
        if (!jwtUtil.validateToken(token) || !jwtUtil.isRefreshToken(token)) {
            throw new AppException(ErrorCode.AUTHENTICATION_ERROR);
        }

        int id = jwtUtil.extractUserId(token);
        String role = findRoleByUserId(id);

        // Lấy token cũ từ DB (chưa revoke)
        UserTokens existing = userTokenService.findTokenByUser(token, id);
        if (existing == null || Boolean.TRUE.equals(existing.getRevoked())) {
            throw new AppException(ErrorCode.TOKEN_HAS_EXPIRED);
        }
        if (existing.getExpires_at().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_HAS_EXPIRED);
        }

        //Sinh cặp token mới
        String newAccessToken = jwtUtil.generateAccessToken(id, role);
        String newRefreshToken = jwtUtil.generateRefreshToken(id);

        //Lưu token mới
        userTokenService.saveToken(UserTokens.builder()
                .user_id(id)
                .token(newRefreshToken)
                .expires_at(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .created_at(LocalDateTime.now())
                .build());

        //Revoke token cũ (dùng object existing đã lấy từ DB)
        boolean revoked = userTokenService.revokeTokenById(existing.getToken_id());
//        if(!revoked) {
//            throw new AppException(ErrorCode.TOKEN_REVOKED);
//        }

        //Trả về token mới
        return LoginDtoResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
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


    @Override
    public void logout(String authHeader) {
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7)
                : null;

        if (token == null) {
            throw new AppException(ErrorCode.AUTHENTICATION_ERROR);
        }

        if(jwtUtil.isAccessToken(token)) {
            int id = jwtUtil.extractUserId(token);
            userTokenService.revokeTokensByUser(id);
        }
        else {
            throw new AppException(ErrorCode.AUTHENTICATION_ERROR);
        }
    }

}
