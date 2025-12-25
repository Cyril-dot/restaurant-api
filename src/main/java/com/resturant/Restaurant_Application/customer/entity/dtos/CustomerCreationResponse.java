package com.resturant.Restaurant_Application.customer.entity.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerCreationResponse {

    private Integer customerId;
    private String customerName;
    private String customerPhoneNumber;
    private String customerEmail;

}
