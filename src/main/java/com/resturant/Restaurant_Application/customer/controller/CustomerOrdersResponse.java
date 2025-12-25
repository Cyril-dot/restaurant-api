package com.resturant.Restaurant_Application.customer.controller;

import com.resturant.Restaurant_Application.customer.entity.dtos.CompleteOrderView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerOrdersResponse {
    private Integer customerId;
    private String customerName;
    private String customerEmail;
    private List<CompleteOrderView> orders;
}
