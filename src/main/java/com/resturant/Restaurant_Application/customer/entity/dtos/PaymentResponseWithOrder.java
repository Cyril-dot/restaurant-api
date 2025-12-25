package com.resturant.Restaurant_Application.customer.entity.dtos;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponseWithOrder {

    private Integer id;
    private BigDecimal amountpaid;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private Integer orderId;
    private List<CompleteOrderView>  completeOrderViews;

}
