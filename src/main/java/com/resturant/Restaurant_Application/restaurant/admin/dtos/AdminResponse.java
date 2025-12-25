package com.resturant.Restaurant_Application.restaurant.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminResponse {
    private Integer id;
    private String name;
    private String email;
    private String phoneNumber;
}
