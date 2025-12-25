package com.resturant.Restaurant_Application.restaurant.admin.service;

import java.math.BigDecimal;

public record RevenueBreakdown(
        BigDecimal inventoryAndExpenditure,
        BigDecimal profit,
        BigDecimal workersPayment
) {}
