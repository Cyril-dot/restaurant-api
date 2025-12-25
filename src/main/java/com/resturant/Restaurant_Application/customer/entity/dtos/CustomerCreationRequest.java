package com.resturant.Restaurant_Application.customer.entity.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerCreationRequest {
    private String name;
    private String phoneNumber;
    private String email;
    private String password;
}
