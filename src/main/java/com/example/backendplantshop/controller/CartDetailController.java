package com.example.backendplantshop.controller;

import com.example.backendplantshop.dto.respones.ApiResponse;
import com.example.backendplantshop.entity.CartDetails;
import com.example.backendplantshop.enums.ErrorCode;
import com.example.backendplantshop.security.annotations.RequireUser;
import com.example.backendplantshop.security.annotations.RequireUserOrAdmin;
import com.example.backendplantshop.service.intf.CartDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cartdetail")
@RequiredArgsConstructor
public class CartDetailController {
    private final CartDetailService cartDetailService;

    // Lấy chi tiết giỏ hàng theo cartId
    @GetMapping("/get-cart-detail/{cartId}")
    @RequireUser
    public ApiResponse<List<CartDetails>> getCartDetailsByCartId(@PathVariable("cartId") int cartId) {
        List<CartDetails> cartDetails = cartDetailService.getCartDetailsByCartId(cartId);
        return ApiResponse.<List<CartDetails>>builder()
                .statusCode(ErrorCode.CALL_API_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.CALL_API_SUCCESSFULL.getMessage())
                .data(cartDetails)
                .build();
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add-product/{userId}/{productId}/{quantity}")
    @RequireUserOrAdmin
    public ApiResponse<Void> addProductToCart(@PathVariable("userId") int userId,
                                              @PathVariable("productId") int productId,
                                              @PathVariable("quantity") int quantity
                                              ) {
        cartDetailService.addProductToCart(userId, productId, quantity);
        return ApiResponse.<Void>builder()
                .statusCode(ErrorCode.CALL_API_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message("Sản phẩm đã được thêm/cập nhật trong giỏ hàng")
                .build();
    }
}
