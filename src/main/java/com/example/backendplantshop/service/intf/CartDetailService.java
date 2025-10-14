package com.example.backendplantshop.service.intf;


import com.example.backendplantshop.entity.CartDetails;

import java.util.List;

public interface CartDetailService {
    // Lấy danh sách chi tiết giỏ hàng theo cartId
    List<CartDetails> getCartDetailsByCartId(int cartId);


    // Thêm sản phẩm vào giỏ (nếu đã tồn tại thì tăng số lượng)
    void addProductToCart(int userId, int productId, int quantity);

    // Cập nhật số lượng trực tiếp
    void updateQuantity(int userId, int productId, int newQuantity, String authHeader);

    // Tăng số lượng lên 1
    void increaseQuantity(int userId, int productId, String authHeader);

    // Giảm số lượng xuống 1
    void decreaseQuantity(int userId, int productId, String authHeader);
}
