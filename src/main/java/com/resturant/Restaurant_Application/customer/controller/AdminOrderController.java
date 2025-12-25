package com.resturant.Restaurant_Application.customer.controller;

import com.resturant.Restaurant_Application.customer.controller.CustomerOrdersResponse;
import com.resturant.Restaurant_Application.customer.entity.dtos.CompleteOrderView;
import com.resturant.Restaurant_Application.customer.entity.dtos.PaymentResponseWithOrder;
import com.resturant.Restaurant_Application.restaurant.admin.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    // ------------------- Orders -------------------

    @GetMapping
    public List<CompleteOrderView> getAllOrders() {
        log.info("Admin request: Get all orders");
        return adminOrderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    public CompleteOrderView getOrderById(@PathVariable Integer orderId) {
        log.info("Admin request: Get order by ID {}", orderId);
        return adminOrderService.getOrderById(orderId);
    }

    @GetMapping("/by-date")
    public List<CompleteOrderView> getOrdersByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Admin request: Get orders by date {}", date);
        return adminOrderService.getOrdersByDate(date);
    }

    @GetMapping("/by-status")
    public List<CompleteOrderView> getOrdersByStatus(@RequestParam String status) {
        log.info("Admin request: Get orders by status {}", status);
        return adminOrderService.getOrdersByStatus(status);
    }

    // ------------------- Payments -------------------

    @GetMapping("/payments")
    public List<PaymentResponseWithOrder> getAllPayments() {
        log.info("Admin request: Get all payments");
        return adminOrderService.getAllPayments();
    }

    @GetMapping("/payments/by-date")
    public List<PaymentResponseWithOrder> getPaymentsByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Admin request: Get payments by date {}", date);
        return adminOrderService.getPaymentsByDate(date);
    }

    @GetMapping("/payments/{orderId}")
    public PaymentResponseWithOrder getPaymentByOrderId(@PathVariable Integer orderId) {
        log.info("Admin request: Get payment for order ID {}", orderId);
        return adminOrderService.getPaymentByOrderId(orderId);
    }

    // ------------------- Customers -------------------

    @GetMapping("/customers")
    public List<CustomerOrdersResponse> getAllCustomersWithOrders() {
        log.info("Admin request: Get all customers with orders");
        return adminOrderService.getAllCustomersWithOrders();
    }
}
