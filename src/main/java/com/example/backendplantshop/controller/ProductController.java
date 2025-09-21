package com.example.backendplantshop.controller;

import com.example.backendplantshop.dto.request.ProductDtoRequest;
import com.example.backendplantshop.dto.respones.ApiResponse;
import com.example.backendplantshop.dto.respones.ProductDtoResponse;
import com.example.backendplantshop.enums.ErrorCode;
import com.example.backendplantshop.security.annotations.RequireAdmin;
import com.example.backendplantshop.security.annotations.RequireUserOrAdmin;
import com.example.backendplantshop.service.intf.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/product")
@AllArgsConstructor
public class ProductController {
    @Autowired
    private final ProductService productService;

    @Autowired
    private ObjectMapper objectMapper; //tạo đối tượng dể chuyển json -> Object jva

    @GetMapping("/getall")
    @RequireUserOrAdmin
    public ApiResponse<List<ProductDtoResponse>> goGetAllProduct() {
        return ApiResponse.<List<ProductDtoResponse>>builder()
                .statusCode(ErrorCode.CALL_API_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.CALL_API_SUCCESSFULL.getMessage())
                .data(productService.getAllProducts())
                .build();
    }

    @GetMapping("getbybid/{id}")
    @RequireUserOrAdmin
    public ApiResponse<ProductDtoResponse> doGetProductById(@PathVariable("id") int id) {
        return ApiResponse.<ProductDtoResponse>builder()
                .statusCode(ErrorCode.CALL_API_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.CALL_API_SUCCESSFULL.getMessage())
                .data(productService.findProductById(id))
                .build();
    }

    @PostMapping(value = "/add")
    @RequireAdmin
    public ApiResponse<Void> doInsertProduct(
            @RequestParam("product") String productJson,  // nhận JSON string
            @RequestPart("image") MultipartFile image) throws IOException { //dùng multipartFile để upload hình ảnh

        // Parse JSON string thành ProductRequest object
        ProductDtoRequest productRequest = objectMapper.readValue(productJson, ProductDtoRequest.class);

        productService.insert(productRequest, image);
        return ApiResponse.<Void>builder()
                .statusCode(ErrorCode.ADD_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.ADD_SUCCESSFULL.getMessage())
                .build();

    }

    @PutMapping("/update/{id}")
    @RequireAdmin
    public ApiResponse<Void> doUpdateProduct(
            @PathVariable("id") int id,
            @RequestParam("product") String productJson,  // nhận JSON string
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {


        // Parse JSON string thành ProductRequest object
        ProductDtoRequest productRequest = objectMapper.readValue(productJson, ProductDtoRequest.class);

        productService.update(id, productRequest, image);
        return ApiResponse.<Void>builder()
                .statusCode(ErrorCode.UPDATE_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.UPDATE_SUCCESSFULL.getMessage())
                .build();

    }

    @DeleteMapping("/delete/{id}")
    @RequireAdmin
    public ApiResponse<Void> doDeleteProduct(@PathVariable("id") int id) {
        productService.delete(id);
        return ApiResponse.<Void>builder()
                .statusCode(ErrorCode.DELETE_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.DELETE_SUCCESSFULL.getMessage())
                .build();
    }

    @PutMapping("/restore/{id}")
    @RequireAdmin
    ApiResponse<Void> restore(@PathVariable("id") int id) {
        productService.restoreProduct(id);
        return ApiResponse.<Void>builder()
                .statusCode(ErrorCode.RESTORE_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.RESTORE_SUCCESSFULL.getMessage())
                .build();
    }
}
