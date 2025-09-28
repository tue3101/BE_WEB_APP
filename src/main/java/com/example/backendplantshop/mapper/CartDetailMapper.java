package com.example.backendplantshop.mapper;

import com.example.backendplantshop.entity.CartDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartDetailMapper {
    // Lấy danh sách chi tiết giỏ hàng theo cartId
    List<CartDetails> findCartDetailByCartId(@Param("cartId") int cartId);

    // Thêm sản phẩm vào giỏ (nếu đã tồn tại thì tăng số lượng)
    void insetProductToCart(@Param("cartId") int cartId,
                            @Param("productId") int productId,
                            @Param("quantity") int quantity);

}
