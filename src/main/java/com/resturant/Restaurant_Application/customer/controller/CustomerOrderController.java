package com.resturant.Restaurant_Application.customer.controller;

import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.customer.entity.Enum.Status;
import com.resturant.Restaurant_Application.customer.entity.dtos.*;
import com.resturant.Restaurant_Application.customer.security.TokenService;
import com.resturant.Restaurant_Application.customer.service.CustomerCreationService;
import com.resturant.Restaurant_Application.customer.service.RestaurantOrderService;
import com.resturant.Restaurant_Application.restaurant.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@PreAuthorize("hasRole('USER')")
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerOrderController {

    private final RestaurantOrderService restaurantOrderService;
    private final CustomerCreationService customerService;
    private final TokenService tokenService;

    private CustomerEntity getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Missing or invalid Authorization header");
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String email = tokenService.getEmailFromAccessToken(token);
        log.info("Authenticated user: {}", email);
        return customerService.getUserByEmail(email);
    }

    // ------------------ MENU ------------------
    @GetMapping("/menu")
    public ResponseEntity<List<MenuResponse>> getMenu(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        CustomerEntity customerEntity = getUserFromToken(authHeader);
        if (customerEntity == null) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        try {
            List<MenuResponse> menu = restaurantOrderService.viewMenu(getUserFromToken(authHeader).getEmail());
            log.info("Fetched restaurant menu: {} items", menu.size());
            return ResponseEntity.ok(menu);
        } catch (Exception e) {
            log.error("Error fetching menu", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/menu/search")
    public ResponseEntity<List<MenuResponse>> searchMenu(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestParam String keyword) {
        CustomerEntity customerEntity = getUserFromToken(authHeader);
        if (customerEntity == null) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        try {
            List<MenuResponse> menu = restaurantOrderService.searchMenuItems(customerEntity.getEmail(), keyword);
            return ResponseEntity.ok(menu);
        } catch (Exception e) {
            log.error("Error fetching menu", e);
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/menu/category")
    public ResponseEntity<List<MenuResponse>> menuCategory(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,@RequestParam String category){
        CustomerEntity customerEntity = getUserFromToken(authHeader);
        if (customerEntity == null) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        try {
            List<MenuResponse> menu = restaurantOrderService.groupByCategory(customerEntity.getEmail(), category);
            return ResponseEntity.ok(menu);
        } catch (Exception e) {
            log.error("Error fetching menu", e);
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/toppings")
    public ResponseEntity<List<ToppingsResponse>> getToppings(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            List<ToppingsResponse> toppings = restaurantOrderService.viewToppings(getUserFromToken(authHeader).getEmail());
            log.info("Fetched toppings: {} items", toppings.size());
            return ResponseEntity.ok(toppings);
        } catch (Exception e) {
            log.error("Error fetching toppings", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/toppings/search")
    public ResponseEntity<List<ToppingsResponse>> userToppingsSearch(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestParam String keyword) {
        CustomerEntity customer = getUserFromToken(authHeader);
        if (customer == null) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        try{
            List<ToppingsResponse> toppings = restaurantOrderService.searchToppings(customer.getEmail(), keyword);
            log.info("Fetched search toppings: {} items", toppings.size());
            return ResponseEntity.ok(toppings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ------------------ ORDERS ------------------
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrders(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            List<OrderResponse> orders = restaurantOrderService.viewOrders(getUserFromToken(authHeader).getEmail());
            log.info("Fetched orders: {} items", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error fetching orders", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/orders/complete")
    public ResponseEntity<List<CompleteOrderView>> getCompleteOrders(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            List<CompleteOrderView> orders = restaurantOrderService.getCompleteOrders(getUserFromToken(authHeader).getEmail());
            log.info("Fetched complete orders: {} items", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error fetching complete orders", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<CompleteOrderView> getOrderById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Integer orderId) {
        try {
            CompleteOrderView order = restaurantOrderService.getOrderById(getUserFromToken(authHeader).getEmail(), orderId);
            log.info("Fetched order with ID: {}", orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error fetching order with ID: {}", orderId, e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/orders/recent")
    public ResponseEntity<List<CompleteOrderView>> getRecentOrders(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            List<CompleteOrderView> recentOrders = restaurantOrderService.getMostRecentOrders(getUserFromToken(authHeader).getEmail());
            log.info("Fetched recent orders: {} items", recentOrders.size());
            return ResponseEntity.ok(recentOrders);
        } catch (Exception e) {
            log.error("Error fetching recent orders", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/orders/date")
    public ResponseEntity<List<CompleteOrderView>> getOrdersByDate(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam LocalDate date) {
        try {
            List<CompleteOrderView> orders = restaurantOrderService.getByOrderDate(getUserFromToken(authHeader).getEmail(), date);
            log.info("Fetched orders for date: {}", date);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error fetching orders by date", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/orders/status")
    public ResponseEntity<List<CompleteOrderView>> getOrdersByStatus(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam Status status) {
        try {
            List<CompleteOrderView> orders = restaurantOrderService.getOrdersByStatus(getUserFromToken(authHeader).getEmail(), status);
            log.info("Fetched orders with status: {}", status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error fetching orders by status", e);
            throw new RuntimeException(e);
        }
    }

    // ------------------ ORDER ITEMS ------------------
    @GetMapping("/orders/items")
    public ResponseEntity<List<OrderItemsResponse>> getOrderItems(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            List<OrderItemsResponse> items = restaurantOrderService.getOrderItems(getUserFromToken(authHeader).getEmail());
            log.info("Fetched order items: {} items", items.size());
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Error fetching order items", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/orders/{orderId}/items")
    public ResponseEntity<List<OrderItemsResponse>> getOrderItemsByOrderId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Integer orderId) {
        try {
            List<OrderItemsResponse> items = restaurantOrderService.getOrderItemsByOrderId(getUserFromToken(authHeader).getEmail(), orderId);
            log.info("Fetched order items for order ID: {}", orderId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Error fetching order items by order ID: {}", orderId, e);
            throw new RuntimeException(e);
        }
    }

    // ------------------ PAYMENTS ------------------
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponseWithOrder>> getPayments(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            List<PaymentResponseWithOrder> payments = restaurantOrderService.viewPaymentRecords(getUserFromToken(authHeader).getEmail());
            log.info("Fetched payments: {} items", payments.size());
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error fetching payments", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/payments/date")
    public ResponseEntity<List<PaymentResponseWithOrder>> getPaymentsByDate(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam LocalDate date) {
        try {
            List<PaymentResponseWithOrder> payments = restaurantOrderService.getPaymentsByDate(getUserFromToken(authHeader).getEmail(), date);
            log.info("Fetched payments for date: {}", date);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error fetching payments by date", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/payments/recent")
    public ResponseEntity<List<PaymentResponseWithOrder>> getRecentPayments(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            List<PaymentResponseWithOrder> payments = restaurantOrderService.getMostRecentPayments(getUserFromToken(authHeader).getEmail());
            log.info("Fetched recent payments: {} items", payments.size());
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error fetching recent payments", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/payments/order/{orderId}")
    public ResponseEntity<PaymentResponseWithOrder> getPaymentByOrderId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Integer orderId) {
        try {
            PaymentResponseWithOrder payment = restaurantOrderService.getByOrderId_payments(getUserFromToken(authHeader).getEmail(), orderId);
            log.info("Fetched payment for order ID: {}", orderId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error fetching payment for order ID: {}", orderId, e);
            throw new RuntimeException(e);
        }
    }


}
