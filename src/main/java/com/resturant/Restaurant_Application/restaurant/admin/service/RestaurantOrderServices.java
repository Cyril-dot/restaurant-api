package com.resturant.Restaurant_Application.restaurant.admin.service;

import com.resturant.Restaurant_Application.ExceptionHandlers.UserDoesNotExistException;
import com.resturant.Restaurant_Application.customer.entity.Enum.Status;
import com.resturant.Restaurant_Application.customer.entity.dtos.MenuResponse;
import com.resturant.Restaurant_Application.customer.entity.dtos.ToppingsResponse;
import com.resturant.Restaurant_Application.restaurant.InRestaurantOrders;
import com.resturant.Restaurant_Application.restaurant.InRestaurantPayments;
import com.resturant.Restaurant_Application.restaurant.Menu;
import com.resturant.Restaurant_Application.restaurant.Toppings;
import com.resturant.Restaurant_Application.restaurant.admin.dtos.*;
import com.resturant.Restaurant_Application.restaurant.admin.repo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantOrderServices {

    private final MenuRepo menuRepo;
    private final ToppingsRepo toppingsRepo;
    private final InRestaurantOrderRepo orderRepo;
    private final InRestaurantPaymentRepo paymentRepo;

    /* ===================== CREATE ORDER ===================== */

    public InRestaurantOrderRestaurantResponse addOrder(InRestaurantOrderRequest request) {

        Menu menu = menuRepo.findByFoodNameIgnoreCase(request.getFoodName());
        if (menu == null || !menu.getIs_available()) {
            throw new IllegalStateException("Menu item is not available");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        List<Toppings> toppings = new ArrayList<>();
        BigDecimal toppingsPrice = BigDecimal.ZERO;

        if (request.getToppingsName() != null) {
            for (String name : request.getToppingsName()) {
                Toppings topping = toppingsRepo.findByNameIgnoreCase(name);
                if (topping == null) {
                    throw new IllegalStateException("Topping not found: " + name);
                }
                if (!topping.getIs_available()) {
                    throw new IllegalStateException("Topping not available: " + name);
                }
                toppings.add(topping);
                toppingsPrice = toppingsPrice.add(topping.getPrice());
            }
        }

        BigDecimal total =
                menu.getPrice()
                        .add(toppingsPrice)
                        .multiply(BigDecimal.valueOf(request.getQuantity()));

        InRestaurantOrders order = InRestaurantOrders.builder()
                .menu(menu)
                .toppings(toppings)
                .quantity(request.getQuantity())
                .price(total)
                .orderStatus(Status.PENDING)
                .orderDate(LocalDateTime.now())
                .build();

        orderRepo.save(order);
        return mapToOrder(order);
    }

    /* ===================== STATUS FLOW ===================== */

    public void confirmOrder(Integer orderId) {
        updateStatus(orderId, Status.PENDING, Status.CONFIRMED);
    }

    public void sendToKitchen(Integer orderId) {
        updateStatus(orderId, Status.CONFIRMED, Status.PREPARING);
    }

    public void markReady(Integer orderId) {
        updateStatus(orderId, Status.PREPARING, Status.READY);
    }

    public void serveOrder(Integer orderId) {
        updateStatus(orderId, Status.READY, Status.SERVED);
    }

    private void updateStatus(Integer orderId, Status expected, Status next) {
        InRestaurantOrders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new UserDoesNotExistException("Order not found"));

        if (order.getOrderStatus() != expected) {
            throw new IllegalStateException("Invalid status transition");
        }

        order.setOrderStatus(next);
        orderRepo.save(order);
    }

    public InRestaurantOrderRestaurantResponse updateOrderStatus(
            Integer orderId,
            Status newStatus
    ) {
        InRestaurantOrders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new UserDoesNotExistException("Order not found"));

        Status currentStatus = order.getOrderStatus();

        // ❌ Prevent changes after completion or cancellation
        if (currentStatus == Status.COMPLETED || currentStatus == Status.CANCELLED) {
            throw new IllegalStateException("Cannot update a completed or cancelled order");
        }

        // ❌ Prevent going backwards
        if (newStatus.ordinal() < currentStatus.ordinal()) {
            throw new IllegalStateException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus
            );
        }

        order.setOrderStatus(newStatus);
        orderRepo.save(order);

        System.out.println(
                "Order ID " + orderId + " status updated from "
                        + currentStatus + " to " + newStatus
        );

        return mapToOrder(order);
    }


    /* ===================== PAYMENT ===================== */

    public InRestaurantPaymentResponse pay(Integer orderId, InRestaurantPayRequest request) {

        InRestaurantOrders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new UserDoesNotExistException("Order not found"));

        if (order.getOrderStatus() != Status.SERVED) {
            throw new IllegalStateException("Order must be served before payment");
        }

        if (request.getAmountPaid().compareTo(order.getPrice()) != 0) {
            throw new IllegalArgumentException("Amount must equal order total");
        }

        // ⚡ Create payment and set FK
        InRestaurantPayments payment = InRestaurantPayments.builder()
                .order(order)                       // link order properly
                .amountPaid(request.getAmountPaid())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(LocalDateTime.now())
                .build();

        paymentRepo.save(payment);

        // Update order status
        order.setOrderStatus(Status.COMPLETED);
        orderRepo.save(order);

        return InRestaurantPaymentResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .amountPaid(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .build();
    }

    /* ===================== UPDATE / DELETE ===================== */

    @Transactional
    public InRestaurantOrderRestaurantResponse updateOrder(
            Integer orderId,
            InRestaurantOrderRequest request) {

        InRestaurantOrders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new UserDoesNotExistException("Order not found"));

        if (order.getOrderStatus() != Status.PENDING) {
            throw new IllegalStateException("Only pending orders can be updated");
        }

        // ===== MENU =====
        Menu menu = menuRepo.findByFoodNameIgnoreCase(request.getFoodName());
        if (menu == null || !menu.getIs_available()) {
            throw new IllegalStateException("Menu item not available");
        }

        // ===== TOPPINGS =====
        List<Toppings> toppings = new ArrayList<>();
        BigDecimal toppingsPrice = BigDecimal.ZERO;

        if (request.getToppingsName() != null) {
            for (String name : request.getToppingsName()) {
                Toppings topping = toppingsRepo.findByNameIgnoreCase(name);
                if (topping == null || !topping.getIs_available()) {
                    throw new IllegalStateException("Topping not available: " + name);
                }
                toppings.add(topping);
                toppingsPrice = toppingsPrice.add(topping.getPrice());
            }
        }

        // ===== QUANTITY =====
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // ===== PRICE RECALCULATION =====
        BigDecimal totalPrice = menu.getPrice()
                .add(toppingsPrice)
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        // ===== UPDATE ORDER =====
        order.setMenu(menu);
        order.setToppings(toppings);
        order.setQuantity(request.getQuantity());
        order.setPrice(totalPrice);

        orderRepo.save(order);

        System.out.println("Updated Order ID: " + order.getId());

        return mapToOrder(order); // SAFE now
    }


    public void deleteOrder(Integer orderId) {
        InRestaurantOrders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new UserDoesNotExistException("Order not found"));

        if (order.getOrderStatus() != Status.PENDING) {
            throw new IllegalStateException("Only pending orders can be deleted");
        }

        orderRepo.delete(order);
    }

    /* ===================== VIEW METHODS ===================== */

    public List<InRestaurantOrderRestaurantResponse> viewAllOrders() {
        return orderRepo.findAll().stream().map(this::mapToOrder).toList();
    }

    public InRestaurantOrderRestaurantResponse viewOrderById(Integer id) {
        return orderRepo.findById(id).map(this::mapToOrder)
                .orElseThrow(() -> new UserDoesNotExistException("Order not found"));
    }

    public List<InRestaurantOrderRestaurantResponse> viewOrdersByStatus(Status status) {
        return orderRepo.findByOrderStatus(status)
                .stream().map(this::mapToOrder).toList();
    }

    public List<InRestaurantOrderRestaurantResponse> viewOrdersToday() {
        LocalDate today = LocalDate.now();
        return orderRepo.findByOrderDateBetween(
                today.atStartOfDay(),
                today.atTime(LocalTime.MAX)
        ).stream().map(this::mapToOrder).toList();
    }

    /* ===================== REPORTS ===================== */

    public BigDecimal dailyTotal(LocalDateTime start, LocalDateTime end) {
        return orderRepo.findByOrderDateBetween(start, end)
                .stream().map(InRestaurantOrders::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public RevenueBreakdown revenueSplit(BigDecimal total) {
        return new RevenueBreakdown(
                total.multiply(BigDecimal.valueOf(0.60)),
                total.multiply(BigDecimal.valueOf(0.10)),
                total.multiply(BigDecimal.valueOf(0.20))
        );
    }

    /* ===================== MAPPER ===================== */

    private InRestaurantOrderRestaurantResponse mapToOrder(InRestaurantOrders order) {

        MenuResponse menu = new MenuResponse(
                order.getMenu().getId(),
                order.getMenu().getFoodName(),
                order.getMenu().getCategory(),
                order.getMenu().getIs_available(),
                order.getMenu().getPrice(),
                order.getMenu().getDescription()
        );

        List<ToppingsResponse> toppings =
                order.getToppings().stream()
                        .map(t -> new ToppingsResponse(t.getId(), t.getName(), t.getPrice()))
                        .toList();

        return InRestaurantOrderRestaurantResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .menu(menu)
                .toppings(toppings)
                .build();
    }

    public ToppingsResponse updateTopping(String toppingName, String newName, BigDecimal newPrice, Boolean isAvailable) {
        Toppings topping = toppingsRepo.findByNameIgnoreCase(toppingName);

        if (topping == null) {
            throw new UserDoesNotExistException("Topping not found: " + toppingName);
        }
        // Update name if provided
        if (newName != null && !newName.isEmpty()) {
            topping.setName(newName);
        }

        // Update price if provided
        if (newPrice != null) {
            topping.setPrice(newPrice);
        }
        // Update availability if provided
        if (isAvailable != null) {
            topping.setIs_available(isAvailable);
        }
        toppingsRepo.save(topping);
        System.out.println("Updated Topping: " + topping.getName() +
                ", Price: " + topping.getPrice() +
                ", Available: " + topping.getIs_available());

        return new ToppingsResponse(
                topping.getId(),
                topping.getName(),
                topping.getPrice()
        );
    }

    /* ===================== MENU MANAGEMENT ===================== */

    /**
     * Add a new menu item
     */
    public MenuResponse addMenuItem(String foodName, String category, BigDecimal price, String description, Boolean isAvailable) {
        Menu existing = menuRepo.findByFoodNameIgnoreCase(foodName);
        if (existing != null) {
            throw new IllegalStateException("Menu item already exists: " + foodName);
        }

        Menu menu = Menu.builder()
                .foodName(foodName)
                .category(category)
                .price(price)
                .description(description)
                .is_available(isAvailable != null ? isAvailable : true)
                .build();

        menuRepo.save(menu);

        System.out.println("Added Menu Item: " + menu.getFoodName() + ", Price: " + menu.getPrice() + ", Available: " + menu.getIs_available());

        return new MenuResponse(menu.getId(), menu.getFoodName(), menu.getCategory(), menu.getIs_available(), menu.getPrice(), menu.getDescription());
    }

    /**
     * Update an existing menu item
     */
    public MenuResponse updateMenuItem(String foodName, BigDecimal newPrice, String newCategory, String newDescription, Boolean isAvailable) {
        Menu menu = menuRepo.findByFoodNameIgnoreCase(foodName);
        if (menu == null) {
            throw new UserDoesNotExistException("Menu item not found: " + foodName);
        }

        if (newPrice != null) menu.setPrice(newPrice);
        if (newCategory != null) menu.setCategory(newCategory);
        if (newDescription != null) menu.setDescription(newDescription);
        if (isAvailable != null) menu.setIs_available(isAvailable);

        menuRepo.save(menu);

        System.out.println("Updated Menu Item: " + menu.getFoodName() + ", Price: " + menu.getPrice() + ", Available: " + menu.getIs_available());

        return new MenuResponse(menu.getId(), menu.getFoodName(), menu.getCategory(), menu.getIs_available(), menu.getPrice(), menu.getDescription());
    }

    /**
     * Remove a menu item
     */
    public void removeMenuItem(String foodName) {
        Menu menu = menuRepo.findByFoodNameIgnoreCase(foodName);
        if (menu == null) {
            throw new UserDoesNotExistException("Menu item not found: " + foodName);
        }

        menuRepo.delete(menu);
        System.out.println("Removed Menu Item: " + foodName);
    }

    /* ===================== TOPPINGS MANAGEMENT ===================== */

    /**
     * Add a new topping
     */
    public ToppingsResponse addTopping(String name, BigDecimal price, Boolean isAvailable) {
        Toppings existing = toppingsRepo.findByNameIgnoreCase(name);
        if (existing != null) {
            throw new IllegalStateException("Topping already exists: " + name);
        }

        Toppings topping = Toppings.builder()
                .name(name)
                .price(price)
                .is_available(isAvailable != null ? isAvailable : true)
                .build();

        toppingsRepo.save(topping);

        System.out.println("Added Topping: " + topping.getName() + ", Price: " + topping.getPrice() + ", Available: " + topping.getIs_available());

        return new ToppingsResponse(topping.getId(), topping.getName(), topping.getPrice());
    }

    /**
     * Remove a topping
     */
    public void removeTopping(String name) {
        Toppings topping = toppingsRepo.findByNameIgnoreCase(name);
        if (topping == null) {
            throw new UserDoesNotExistException("Topping not found: " + name);
        }

        // Instead of deleting, mark as unavailable
        topping.setIs_available(false);
        toppingsRepo.save(topping);

        System.out.println("Topping '" + name + "' has been disabled (unavailable) instead of deleted.");
    }


}
