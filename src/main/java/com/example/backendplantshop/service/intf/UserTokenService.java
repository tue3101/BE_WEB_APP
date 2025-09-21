package com.example.backendplantshop.service.intf;

import com.example.backendplantshop.entity.UserTokens;

import java.util.List;

public interface UserTokenService {
    void saveToken(UserTokens token);
    void revokeTokenById(int tokenId);
    void revokeTokensByUser(int userId);
    UserTokens findByToken(String token);
    UserTokens findById(int tokenId);
    List<UserTokens> findValidTokensByUser(int userId);
}