package com.resturant.Restaurant_Application.customer.service;

import com.resturant.Restaurant_Application.ExceptionHandlers.UserDoesNotExistException;
import com.resturant.Restaurant_Application.customer.entity.*;
import com.resturant.Restaurant_Application.customer.entity.Enum.Status;
import com.resturant.Restaurant_Application.customer.entity.dtos.*;
import com.resturant.Restaurant_Application.customer.entity.repo.*;
import com.resturant.Restaurant_Application.restaurant.Menu;
import com.resturant.Restaurant_Application.restaurant.Toppings;
import com.resturant.Restaurant_Application.restaurant.admin.repo.MenuRepo;
import com.resturant.Restaurant_Application.restaurant.admin.repo.ToppingsRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderPlacementService {

    private final OrderItemsRepo orderItemsRepo;
    private final MenuRepo menuRepo;
    private final ToppingsRepo toppingsRepo;
    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;
    private final CustomerCreationService service;
    private final NotificationService notificationService;

    /** ------------------ PLACE ORDER ------------------ **/
    public OrderResponse placeOrder(String email) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UsernameNotFoundException("Customer does not exist");

        // Delete empty orders first
        orderRepo.deleteOrdersWithoutItems();

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setDate(LocalDateTime.now());
        order.setStatus(Status.PENDING);
        orderRepo.save(order);

        // Notify admin
        notificationService.createNotification(
                "New Order Placed",
                "Order #" + order.getId() + " has been placed by " + customer.getName() + "."
        );

        return OrderResponse.builder()
                .id(order.getId())
                .date(order.getDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .build();
    }

    /** ------------------ ADD ORDER ITEMS ------------------ **/
    public CompleteOrderView addOrderItems(String email, Integer orderId, OrderItemsRequest request){
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UsernameNotFoundException("Customer does not exist");

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new UserDoesNotExistException("Order does not exist"));

        // Fetch menu item
        Menu menuItem = menuRepo.findByFoodNameIgnoreCase(request.getFoodName());
        if (menuItem == null) throw new UserDoesNotExistException("Dish not found");
        BigDecimal menuPrice = menuItem.getPrice();

        // Fetch toppings
        List<Toppings> toppingsList = new ArrayList<>();
        BigDecimal toppingsTotalPrice = BigDecimal.ZERO;
        if (request.getToppingsName() != null) {
            for (String toppingName : request.getToppingsName()) {
                Toppings topping = toppingsRepo.findByNameIgnoreCase(toppingName);
                if (topping == null) throw new UserDoesNotExistException("Topping not found: " + toppingName);
                toppingsList.add(topping);
                toppingsTotalPrice = toppingsTotalPrice.add(topping.getPrice());
            }
        }

        // Calculate total for this item
        int quantity = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;
        BigDecimal finalTotal = (menuPrice.add(toppingsTotalPrice)).multiply(BigDecimal.valueOf(quantity));

        // Create OrderItems
        OrderItems placedOrder = OrderItems.builder()
                .order(order)        // link to order
                .menu(menuItem)
                .toppings(toppingsList)
                .quantity(quantity)
                .price(finalTotal)
                .build();
        orderItemsRepo.save(placedOrder);

        // Maintain bidirectional relationship
        order.getOrderItems().add(placedOrder);

        // Update order total
        BigDecimal orderTotal = order.getOrderItems().stream()
                .map(OrderItems::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(orderTotal);
        orderRepo.save(order);

        // Notify admin
        String items = menuItem.getFoodName() + " x" + quantity;
        if (!toppingsList.isEmpty()) {
            items += " (Toppings: " + toppingsList.stream()
                    .map(Toppings::getName)
                    .collect(Collectors.joining(", ")) + ")";
        }
        notificationService.createNotification(
                "Order Items Added",
                "Customer " + customer.getName() + " added items to Order #" + order.getId() + ": " + items
        );

        return mapOrderToCompleteOrderView(order);
    }

    /** ------------------ UPDATE ORDER ITEM ------------------ **/
    public CompleteOrderView updateOrderItem(String email, Integer orderItemId, OrderItemsRequest request) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UsernameNotFoundException("Customer does not exist");

        OrderItems orderItem = orderItemsRepo.findById(orderItemId)
                .orElseThrow(() -> new UserDoesNotExistException("Order item does not exist"));

        Order order = orderItem.getOrder();
        if (order.getStatus() == Status.COMPLETED) {
            throw new IllegalStateException("Cannot update order items for a completed order");
        }

        // Update menu
        if (request.getFoodName() != null && !request.getFoodName().isEmpty()) {
            Menu menuItem = menuRepo.findByFoodNameIgnoreCase(request.getFoodName());
            if (menuItem == null) throw new UserDoesNotExistException("Dish not found");
            orderItem.setMenu(menuItem);
        }

        // Update toppings
        if (request.getToppingsName() != null) {
            List<Toppings> toppingsList = new ArrayList<>();
            BigDecimal toppingsTotalPrice = BigDecimal.ZERO;
            for (String toppingName : request.getToppingsName()) {
                Toppings topping = toppingsRepo.findByNameIgnoreCase(toppingName);
                if (topping == null) throw new UserDoesNotExistException("Topping not found: " + toppingName);
                toppingsList.add(topping);
                toppingsTotalPrice = toppingsTotalPrice.add(topping.getPrice());
            }
            orderItem.setToppings(toppingsList);

            BigDecimal menuPrice = orderItem.getMenu().getPrice();
            int quantity = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : orderItem.getQuantity();
            orderItem.setQuantity(quantity);
            orderItem.setPrice(menuPrice.add(toppingsTotalPrice).multiply(BigDecimal.valueOf(quantity)));
        }

        orderItemsRepo.save(orderItem);

        // Update order total
        BigDecimal orderTotal = order.getOrderItems().stream()
                .map(OrderItems::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(orderTotal);
        orderRepo.save(order);

        // Notify admin
        notificationService.createNotification(
                "Order Item Updated",
                "Customer " + customer.getName() + " updated items in Order #" + order.getId()
        );

        return mapOrderToCompleteOrderView(order);
    }

    /** ------------------ DELETE ORDER ------------------ **/
    public void deleteOrder(String email, Integer orderId) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UsernameNotFoundException("Customer does not exist");

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new UserDoesNotExistException("Order does not exist"));

        orderItemsRepo.deleteAll(order.getOrderItems());
        orderRepo.delete(order);

        // Notify admin
        notificationService.createNotification(
                "Order Cancelled",
                "Customer " + customer.getName() + " deleted Order #" + order.getId()
        );
    }

    /** ------------------ MAKE PAYMENT ------------------ **/
    @Transactional
    public PaymentResponse makePayment(String email, Integer orderId, PaymentRequest request) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UsernameNotFoundException("Customer does not exist");

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new UserDoesNotExistException("Order does not exist"));

        if (order.getStatus() == Status.COMPLETED) throw new IllegalStateException("Order already paid");

        BigDecimal orderTotal = order.getTotalAmount();
        BigDecimal amountPaid = request.getAmountPaid();

        if (amountPaid == null || amountPaid.compareTo(orderTotal) != 0) {
            throw new IllegalArgumentException("Payment must equal the order total");
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(order);
        payment.setAmountpaid(amountPaid);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepo.save(payment);

        order.setStatus(Status.COMPLETED);
        orderRepo.save(order);

        // Notify admin
        notificationService.createNotification(
                "Order Paid",
                "Customer " + customer.getName() + " paid Order #" + order.getId() + ". Amount: " + amountPaid
        );

        return PaymentResponse.builder()
                .orderId(order.getId())
                .amountPaid(payment.getAmountpaid())
                .status(order.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .date(payment.getPaymentDate())
                .build();
    }

    /** ------------------ MAP ORDER TO COMPLETE VIEW ------------------ **/
    private CompleteOrderView mapOrderToCompleteOrderView(Order order) {
        List<OrderItemsResponse> orderItems = order.getOrderItems().stream().map(item -> {
            List<ToppingsResponse> toppings = item.getToppings().stream()
                    .map(t -> new ToppingsResponse(t.getId(), t.getName(), t.getPrice()))
                    .collect(Collectors.toList());

            MenuResponse menuResponse = new MenuResponse(
                    item.getMenu().getId(),
                    item.getMenu().getFoodName(),
                    item.getMenu().getCategory(),
                    item.getMenu().getIs_available(),
                    item.getMenu().getPrice(),
                    item.getMenu().getDescription()
            );

            return OrderItemsResponse.builder()
                    .menuItem(menuResponse)
                    .toppings(toppings)
                    .quantity(item.getQuantity())
                    .totalPrice(item.getPrice())
                    .build();
        }).collect(Collectors.toList());

        return CompleteOrderView.builder()
                .date(order.getDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .orderItems(orderItems)
                .build();
    }
}
