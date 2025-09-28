package com.example.backendplantshop.mapper;

import com.example.backendplantshop.entity.Carts;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CartMapper {
    Carts findCartByUserId(@Param("userID") int userId);
    Integer findCartIdByUserId(@Param("userId") int userId);

    void createCartForUser(@Param("userId") int userId);
}
