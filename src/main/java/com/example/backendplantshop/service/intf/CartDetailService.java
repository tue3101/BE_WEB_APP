package com.example.backendplantshop.service.intf;


import com.example.backendplantshop.entity.CartDetails;

import java.util.List;

public interface CartDetailService {
    // Lấy danh sách chi tiết giỏ hàng theo cartId
    List<CartDetails> getCartDetailsByCartId(int cartId);

    // Thêm sản phẩm vào giỏ (nếu đã tồn tại thì tăng số lượng)
    void addProductToCart(int userId, int productId, int quantity);
}
