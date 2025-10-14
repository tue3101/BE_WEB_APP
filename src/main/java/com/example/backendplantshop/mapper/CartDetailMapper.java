package com.example.backendplantshop.mapper;

import com.example.backendplantshop.entity.CartDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartDetailMapper {
    // Lấy số lượng tồn kho của sản phẩm
    Integer findProductQuantityInStock(@Param("productID") int productId);
    // Lấy danh sách chi tiết giỏ hàng theo cartId
    List<CartDetails> findCartDetailByCartId(@Param("cartID") int cartId);

    // Thêm sản phẩm vào giỏ (nếu đã tồn tại thì tăng số lượng)
    void insetProductToCart(@Param("cartID") int cartId,
                            @Param("productID") int productId,
                            @Param("quantity") int quantity);

    // Cập nhật số lượng trực tiếp
    void updateQuantity(@Param("cartID") int cartId,
                        @Param("productID") int productId,
                        @Param("quantity") int quantity);

    // Tăng số lượng lên 1
    void increaseQuantity(@Param("cartID") int cartId,
                         @Param("productID") int productId);

    // Giảm số lượng xuống 1
    void decreaseQuantity(@Param("cartID") int cartId,
                         @Param("productID") int productId);

    void deleteByUserId(@Param("userID") int userId);
    void restoreByUserId(@Param("userID") int userId);


    Integer findQuantityInCart(Integer cartId, int productId);
}
