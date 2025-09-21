package com.example.backendplantshop.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Carts {
    private int cart_id;
    private int user_id;
    private int is_deleted;

//    @JsonBackReference
    private Users users;
//    @JsonManagedReference
    private List<CartDetails> cart_details;
}
