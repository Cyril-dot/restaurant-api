package com.resturant.Restaurant_Application.customer.entity.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerUpdateRequest {
    private String name;
    private String email;
    private String phoneNumber;
}
