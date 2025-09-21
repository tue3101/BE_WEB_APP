package com.example.backendplantshop.mapper;

import com.example.backendplantshop.entity.Users;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    Users findByEmail(String email);
    Users findByPhoneNumber(String phoneNumber);
    Users findByUsername(String username);
    Users findById(@Param("userID")int id);
    public List<Users> findAll();
    void insert(Users user);
    void update(Users user);
    void delete(@Param("userID")int id);

    Users findByIdDeleted(@Param("userID")int id);
    void restoreUser(@Param("userID")int id);
    int changePassword(@Param("userID") int id,
                       @Param("newPassword") String newPassword);
}
