package com.resturant.Restaurant_Application.customer.controller;

import com.resturant.Restaurant_Application.ExceptionHandlers.UserDoesNotExistException;
import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.customer.entity.dtos.*;
import com.resturant.Restaurant_Application.customer.security.TokenService;
import com.resturant.Restaurant_Application.customer.service.CustomerCreationService;
import com.resturant.Restaurant_Application.customer.service.OrderPlacementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@RequestMapping("/api/customer/orders")

public class OrderPlacementController {
    private final CustomerCreationService service;
    private final TokenService tokenService;
    private final OrderPlacementService orderService;

    private CustomerEntity getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String email = tokenService.getEmailFromAccessToken(token);
        return service.getUserByEmail(email);
    }

    // to place order
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        CustomerEntity customerEntity = getUserFromToken(authHeader);
        if (customerEntity == null) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        try {
            log.info("Placing new order for customer: {}", customerEntity.getEmail());
            OrderResponse response = orderService.placeOrder(customerEntity.getEmail());
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            log.error("Customer not found: {}",  customerEntity.getEmail(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error placing order for customer: {}", customerEntity.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to place order");
        }
    }

    // ---------------- Add order items ----------------
    @PostMapping("/{orderId}/add-items")
    public ResponseEntity<?> addOrderItems(
            @PathVariable Integer orderId,
            @RequestBody OrderItemsRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        CustomerEntity customerEntity = getUserFromToken(authHeader);
        if (customerEntity == null) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        try {
            log.info("Adding items to order {} for customer {}", orderId, customerEntity.getEmail());
            CompleteOrderView response = orderService.addOrderItems(customerEntity.getEmail(), orderId, request);
            return ResponseEntity.ok(response);
        } catch (UserDoesNotExistException | UsernameNotFoundException e) {
            log.error("Error adding order items: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error adding order items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add order items");
        }
    }


    // ---------------- Update order item ----------------
    @PutMapping("/update-item/{orderItemId}")
    public ResponseEntity<?> updateOrderItem(
            @PathVariable Integer orderItemId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody OrderItemsRequest request) {
        CustomerEntity customerEntity = getUserFromToken(authHeader);
        if (customerEntity == null) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        try {
            log.info("Updating order item {} for customer {}", orderItemId, customerEntity.getEmail());
            CompleteOrderView response = orderService.updateOrderItem(customerEntity.getEmail(), orderItemId, request);
            return ResponseEntity.ok(response);
        } catch (UserDoesNotExistException | UsernameNotFoundException e) {
            log.error("Error updating order item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating order item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update order item");
        }
    }

    // ---------------- Delete order ----------------
    @DeleteMapping("/delete/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Integer orderId,  @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        CustomerEntity customerEntity = getUserFromToken(authHeader);
        if (customerEntity == null) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        try {
            log.info("Deleting order {} for customer {}", orderId, customerEntity.getEmail());
            orderService.deleteOrder(customerEntity.getEmail(), orderId);
            return ResponseEntity.ok("Order deleted successfully");
        } catch (UserDoesNotExistException | UsernameNotFoundException e) {
            log.error("Error deleting order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete order");
        }
    }

    // ---------------- Make payment ----------------
    @PostMapping("/payment/{orderId}")
    public ResponseEntity<?> makePayment(
            @PathVariable Integer orderId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody PaymentRequest request) {
        CustomerEntity customerEntity = getUserFromToken(authHeader);
        if (customerEntity == null) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        try {
            log.info("Processing payment for order {} for customer {}", orderId, customerEntity.getEmail());
            PaymentResponse response = orderService.makePayment(customerEntity.getEmail(), orderId, request);
            return ResponseEntity.ok(response);
        } catch (UserDoesNotExistException | UsernameNotFoundException e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Payment validation failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process payment");
        }
    }
}
