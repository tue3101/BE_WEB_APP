package com.example.backendplantshop.service.impl;

import com.example.backendplantshop.entity.CartDetails;
import com.example.backendplantshop.entity.Carts;
import com.example.backendplantshop.enums.ErrorCode;
import com.example.backendplantshop.exception.AppException;
import com.example.backendplantshop.mapper.CartDetailMapper;
import com.example.backendplantshop.mapper.CartMapper;
import com.example.backendplantshop.mapper.UserMapper;
import com.example.backendplantshop.security.JwtUtil;
import com.example.backendplantshop.service.intf.CartDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartDetailServiceImpl implements CartDetailService {
    private final CartDetailMapper cartDetailMapper;
    private final CartMapper cartMapper;
    private final AuthServiceImpl authService;

    @Override
    public List<CartDetails> getCartDetailsByCartId(int cartId) {
        return cartDetailMapper.findCartDetailByCartId(cartId);
    }

    @Override
    public void addProductToCart(int userId, int productId, int quantity) {
        int currentUserId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();
        if (!authService.isUser(role) || currentUserId != userId) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        // 1. Kiểm tra cart của user
        Integer cartId = cartMapper.findCartIdByUserId(userId);

        if (cartId == null) {
            // chưa có giỏ hàng → tạo mới
            cartMapper.createCartForUser(userId);
            cartId = cartMapper.findCartIdByUserId(userId);
        }

        // 2. Kiểm tra số lượng tồn kho của sản phẩm
        Integer availableQuantity = cartDetailMapper.findProductQuantityInStock(productId);
        if (availableQuantity == null || quantity > availableQuantity) {
            throw new AppException(ErrorCode.QUANTITY_IS_NOT_ENOUGH);
        }

        // 3. Lấy số lượng hiện có trong giỏ hàng của user (nếu có)
        Integer currentQuantityInCart = cartDetailMapper.findQuantityInCart(cartId, productId);
        if (currentQuantityInCart == null) {
            currentQuantityInCart = 0;
        }

        // 4. Kiểm tra tổng sau khi cộng thêm
        int totalAfterAdd = currentQuantityInCart + quantity;
        if (totalAfterAdd > availableQuantity) {
            throw new AppException(ErrorCode.QUANTITY_IS_NOT_ENOUGH);
        }

        cartDetailMapper.insetProductToCart(cartId, productId, quantity);
    }

    @Override
    public void updateQuantity(int userId, int productId, int newQuantity, String authHeader) {
        int currentUserId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();
        if (!authService.isUser(role) || currentUserId != userId) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        Integer cartId = cartMapper.findCartIdByUserId(currentUserId);
        if (cartId == null) throw new AppException(ErrorCode.CART_IS_EMPTY);
        Integer availableQuantity = cartDetailMapper.findProductQuantityInStock(productId);
        if (availableQuantity == null || newQuantity > availableQuantity || newQuantity < 1) {
            throw new AppException(ErrorCode.QUANTITY_IS_NOT_ENOUGH);
        }
        cartDetailMapper.updateQuantity(cartId, productId, newQuantity);
    }

    @Override
    public void increaseQuantity(int userId, int productId, String authHeader) {
        int currentUserId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();
        if (!authService.isUser(role) || currentUserId != userId) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        Integer cartId = cartMapper.findCartIdByUserId(currentUserId);
        if (cartId == null){
            throw new AppException(ErrorCode.CART_IS_EMPTY);
        }
        // Lấy số lượng hiện tại
        List<CartDetails> details = cartDetailMapper.findCartDetailByCartId(cartId);
        CartDetails detail = details.stream()
                .filter(d -> d.getProduct_id() == productId)
                .findFirst()
                .orElse(null);
        if (detail == null) throw new AppException(ErrorCode.CART_IS_EMPTY);
        int newQuantity = detail.getQuantity() + 1;
        Integer availableQuantity = cartDetailMapper.findProductQuantityInStock(productId);
        if (availableQuantity == null || newQuantity > availableQuantity) {
            throw new AppException(ErrorCode.QUANTITY_IS_NOT_ENOUGH);
        }
        cartDetailMapper.increaseQuantity(cartId, productId);
    }

    @Override
    public void decreaseQuantity(int userId, int productId, String authHeader) {
        int currentUserId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();
        if (!authService.isUser(role) || currentUserId != userId) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        Integer cartId = cartMapper.findCartIdByUserId(currentUserId);
        if (cartId == null) {
            throw new AppException(ErrorCode.CART_IS_EMPTY);
        }
        // Lấy số lượng hiện tại
        List<CartDetails> details = cartDetailMapper.findCartDetailByCartId(cartId);
        CartDetails detail = details.stream()
                .filter(d -> d.getProduct_id() == productId)
                .findFirst()//trả phần tử product_id ngay lập tức
                .orElse(null);
        if (detail == null || detail.getQuantity() <1)
        {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }
        cartDetailMapper.decreaseQuantity(cartId, productId);
    }
}
