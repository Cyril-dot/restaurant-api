package com.resturant.Restaurant_Application.customer.controller;

import com.resturant.Restaurant_Application.customer.entity.Enum.Status;
import com.resturant.Restaurant_Application.customer.entity.dtos.MenuResponse;
import com.resturant.Restaurant_Application.customer.entity.dtos.ToppingsResponse;
import com.resturant.Restaurant_Application.restaurant.admin.dtos.InRestaurantOrderRequest;
import com.resturant.Restaurant_Application.restaurant.admin.dtos.InRestaurantOrderRestaurantResponse;
import com.resturant.Restaurant_Application.restaurant.admin.dtos.InRestaurantPayRequest;
import com.resturant.Restaurant_Application.restaurant.admin.dtos.InRestaurantPaymentResponse;
import com.resturant.Restaurant_Application.restaurant.admin.service.RestaurantOrderServices;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
@RequiredArgsConstructor
public class RestaurantAdminController {

    private final RestaurantOrderServices orderService;
    private static final Logger logger = LoggerFactory.getLogger(RestaurantAdminController.class);

    /* ===================== ORDERS ===================== */

    @PostMapping("/orders")
    public InRestaurantOrderRestaurantResponse addOrder(@RequestBody InRestaurantOrderRequest request) {
        logger.info("Adding new order: {}", request);
        return orderService.addOrder(request);
    }

    @PutMapping("/orders/{orderId}")
    public InRestaurantOrderRestaurantResponse updateOrder(@PathVariable Integer orderId,
                                                           @RequestBody InRestaurantOrderRequest request) {
        logger.info("Updating order id {} with {}", orderId, request);
        return orderService.updateOrder(orderId, request);
    }

    @DeleteMapping("/orders/{orderId}")
    public void deleteOrder(@PathVariable Integer orderId) {
        logger.info("Deleting order with id {}", orderId);
        orderService.deleteOrder(orderId);
    }

    @PostMapping("/orders/{orderId}/pay")
    public InRestaurantPaymentResponse payOrder(@PathVariable Integer orderId,
                                                @RequestBody InRestaurantPayRequest request) {
        logger.info("Processing payment for order id {}: {}", orderId, request);
        return orderService.pay(orderId, request);
    }

    /* ===================== MENU MANAGEMENT ===================== */

    @PostMapping("/menu")
    public MenuResponse addMenu(@RequestParam String foodName,
                                @RequestParam String category,
                                @RequestParam BigDecimal price,
                                @RequestParam String description,
                                @RequestParam(required = false) Boolean isAvailable) {
        logger.info("Adding menu item: {}", foodName);
        return orderService.addMenuItem(foodName, category, price, description, isAvailable);
    }

    @PutMapping("/menu")
    public MenuResponse updateMenu(@RequestParam String foodName,
                                   @RequestParam(required = false) BigDecimal price,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) String description,
                                   @RequestParam(required = false) Boolean isAvailable) {
        logger.info("Updating menu item: {}", foodName);
        return orderService.updateMenuItem(foodName, price, category, description, isAvailable);
    }

    @DeleteMapping("/menu")
    public void removeMenu(@RequestParam String foodName) {
        logger.info("Removing menu item: {}", foodName);
        orderService.removeMenuItem(foodName);
    }

    /* ===================== TOPPINGS MANAGEMENT ===================== */

    @PostMapping("/toppings")
    public ToppingsResponse addTopping(@RequestParam String name,
                                       @RequestParam BigDecimal price,
                                       @RequestParam(required = false) Boolean isAvailable) {
        logger.info("Adding topping: {}", name);
        return orderService.addTopping(name, price, isAvailable);
    }

    @PutMapping("/toppings")
    public ToppingsResponse updateTopping(@RequestParam String name,
                                          @RequestParam(required = false) String newName,
                                          @RequestParam(required = false) BigDecimal price,
                                          @RequestParam(required = false) Boolean isAvailable) {
        logger.info("Updating topping: {}", name);
        return orderService.updateTopping(name, newName, price, isAvailable);
    }


    @DeleteMapping("/toppings")
    public void removeTopping(@RequestParam String name) {
        logger.info("Removing topping: {}", name);
        orderService.removeTopping(name);
    }

    /**
     * Update order status (Admin only)
     * Example:
     * PUT /admin/orders/5/status?status=READY
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<InRestaurantOrderRestaurantResponse> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam Status status
    ) {
        InRestaurantOrderRestaurantResponse response = orderService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(response);
    }

    /* ===================== ORDERS VIEW ===================== */

    @GetMapping("/orders")
    public List<InRestaurantOrderRestaurantResponse> viewAllOrders() {
        logger.info("Fetching all orders");
        return orderService.viewAllOrders();
    }

    @GetMapping("/orders/{orderId}")
    public InRestaurantOrderRestaurantResponse viewOrderById(@PathVariable Integer orderId) {
        logger.info("Fetching order id {}", orderId);
        return orderService.viewOrderById(orderId);
    }

    @GetMapping("/orders/status")
    public List<InRestaurantOrderRestaurantResponse> viewOrdersByStatus(@RequestParam Status status) {
        logger.info("Fetching orders with status {}", status);
        return orderService.viewOrdersByStatus(status);
    }

    @GetMapping("/orders/today")
    public List<InRestaurantOrderRestaurantResponse> viewOrdersToday() {
        logger.info("Fetching today's orders");
        return orderService.viewOrdersToday();
    }
}
