package com.example.backendplantshop.service.impl;

import com.example.backendplantshop.entity.CartDetails;
import com.example.backendplantshop.entity.Carts;
import com.example.backendplantshop.mapper.CartDetailMapper;
import com.example.backendplantshop.mapper.CartMapper;
import com.example.backendplantshop.mapper.UserMapper;
import com.example.backendplantshop.service.intf.CartDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CartDetailServiceImpl implements CartDetailService {
    private final CartDetailMapper cartDetailMapper;
    private final CartMapper cartMapper; // để check / tạo cart
    private final UserMapper userMapper;

    @Override
    public List<CartDetails> getCartDetailsByCartId(int cartId) {
        return cartDetailMapper.findCartDetailByCartId(cartId);
    }

    @Override
    public void addProductToCart(int userId, int productId, int quantity) {
        // 1. Kiểm tra cart của user
        Integer cartId = cartMapper.findCartIdByUserId(userId);

        if (cartId == null) {
            // chưa có giỏ hàng → tạo mới
            cartMapper.createCartForUser(userId);
            cartId = cartMapper.findCartIdByUserId(userId);
        }

        cartDetailMapper.insetProductToCart(cartId, productId, quantity);
    }
}
