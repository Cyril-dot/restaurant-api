package com.resturant.Restaurant_Application.restaurant.admin.service;

import com.resturant.Restaurant_Application.ExceptionHandlers.UserDoesNotExistException;
import com.resturant.Restaurant_Application.customer.controller.CustomerOrdersResponse;
import com.resturant.Restaurant_Application.customer.entity.Order;
import com.resturant.Restaurant_Application.customer.entity.OrderItems;
import com.resturant.Restaurant_Application.customer.entity.PaymentEntity;
import com.resturant.Restaurant_Application.customer.entity.dtos.*;
import com.resturant.Restaurant_Application.customer.entity.repo.OrderItemsRepo;
import com.resturant.Restaurant_Application.customer.entity.repo.OrderRepo;
import com.resturant.Restaurant_Application.customer.entity.repo.PaymentRepo;
import com.resturant.Restaurant_Application.restaurant.Menu;
import com.resturant.Restaurant_Application.restaurant.Toppings;
import com.resturant.Restaurant_Application.restaurant.admin.repo.MenuRepo;
import com.resturant.Restaurant_Application.restaurant.admin.repo.ToppingsRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminOrderService {

    private final OrderItemsRepo orderItemsRepo;
    private final MenuRepo menuRepo;
    private final ToppingsRepo toppingsRepo;
    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;

    // ------------------- Orders -------------------

    public List<CompleteOrderView> getAllOrders() {
        log.info("Fetching all orders...");
        List<Order> orders = orderRepo.findAllOrdersOrderByDateDesc();
        if (orders.isEmpty()) throw new UserDoesNotExistException("No orders found");
        return mapOrdersToCompleteOrderView(orders);
    }

    public CompleteOrderView getOrderById(Integer orderId) {
        log.info("Fetching order by ID: {}", orderId);
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new UserDoesNotExistException("Order not found"));
        return mapOrdersToCompleteOrderView(List.of(order)).get(0);
    }

    public List<CompleteOrderView> getOrdersByDate(LocalDate date) {
        log.info("Fetching orders by date: {}", date);
        List<Order> orders = orderRepo.findAllByOrderDate(date);
        if (orders.isEmpty()) throw new UserDoesNotExistException("No orders on this date");
        return mapOrdersToCompleteOrderView(orders);
    }

    public List<CompleteOrderView> getOrdersByStatus(String status) {
        log.info("Fetching orders by status: {}", status);
        List<Order> orders = orderRepo.findAllByStatus(status.toUpperCase());
        if (orders.isEmpty()) throw new UserDoesNotExistException("No orders with status: " + status);
        return mapOrdersToCompleteOrderView(orders);
    }

    // ------------------- Payments -------------------

    public List<PaymentResponseWithOrder> getAllPayments() {
        log.info("Fetching all payments...");
        List<PaymentEntity> payments = paymentRepo.findAll();
        if (payments.isEmpty()) throw new UserDoesNotExistException("No payments found");
        return mapToCustomerPayments(payments);
    }

    public PaymentResponseWithOrder getPaymentByOrderId(Integer orderId) {
        log.info("Fetching payment for order ID: {}", orderId);
        PaymentEntity payment = paymentRepo.findByOrderId(orderId);
        if (payment == null) throw new UserDoesNotExistException("No payment found for order " + orderId);
        return mapToCustomerPayments(List.of(payment)).get(0);
    }

    public List<PaymentResponseWithOrder> getPaymentsByDate(LocalDate date) {
        log.info("Fetching payments by date: {}", date);
        List<PaymentEntity> payments = paymentRepo.findByPaymentDate(date);
        if (payments.isEmpty()) throw new UserDoesNotExistException("No payments on this date");
        return mapToCustomerPayments(payments);
    }

    // ------------------- Customers -------------------

    public List<CustomerOrdersResponse> getAllCustomersWithOrders() {
        log.info("Fetching all customers with orders...");
        List<Order> orders = orderRepo.findAll();
        if (orders.isEmpty()) throw new UserDoesNotExistException("No orders found");

        List<CustomerOrdersResponse> response = new ArrayList<>();
        orders.stream().map(Order::getCustomer).distinct().forEach(customer -> {
            List<Order> customerOrders = orderRepo.findByCustomer_Id(customer.getId());
            List<CompleteOrderView> completeOrders = mapOrdersToCompleteOrderView(customerOrders);

            response.add(CustomerOrdersResponse.builder()
                    .customerId(customer.getId())
                    .customerEmail(customer.getEmail())
                    .customerName(customer.getName())
                    .orders(completeOrders)
                    .build());
        });

        return response;
    }

    // ------------------- Private Mapping -------------------

    private List<CompleteOrderView> mapOrdersToCompleteOrderView(List<Order> orders) {
        List<CompleteOrderView> completeOrders = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItemsResponse> orderItems = order.getOrderItems().stream().map(item -> {
                List<ToppingsResponse> toppings = item.getToppings().stream()
                        .map(t -> new ToppingsResponse(t.getId(), t.getName(), t.getPrice()))
                        .collect(Collectors.toList());

                Menu menu = item.getMenu();
                MenuResponse menuResponse = new MenuResponse(
                        menu.getId(),
                        menu.getFoodName(),
                        menu.getCategory(),
                        menu.getIs_available(),
                        menu.getPrice(),
                        menu.getDescription()
                );

                return OrderItemsResponse.builder()
                        .menuItem(menuResponse)
                        .toppings(toppings)
                        .quantity(item.getQuantity())
                        .totalPrice(item.getPrice())
                        .build();
            }).collect(Collectors.toList());

            CompleteOrderView completeOrder = CompleteOrderView.builder()
                    .date(order.getDate())
                    .status(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .orderItems(orderItems)
                    .build();

            completeOrders.add(completeOrder);
        }
        return completeOrders;
    }

    private List<PaymentResponseWithOrder> mapToCustomerPayments(List<PaymentEntity> payments) {
        List<PaymentResponseWithOrder> response = new ArrayList<>();
        for (PaymentEntity payment : payments) {
            Order order = payment.getOrder();
            List<OrderItemsResponse> orderItemsResponses = order.getOrderItems().stream().map(item -> {
                Menu menu = item.getMenu();
                MenuResponse menuResponse = new MenuResponse(
                        menu.getId(),
                        menu.getFoodName(),
                        menu.getCategory(),
                        menu.getIs_available(),
                        menu.getPrice(),
                        menu.getDescription()
                );
                List<ToppingsResponse> toppings = item.getToppings().stream()
                        .map(t -> new ToppingsResponse(t.getId(), t.getName(), t.getPrice()))
                        .toList();

                return OrderItemsResponse.builder()
                        .menuItem(menuResponse)
                        .toppings(toppings)
                        .quantity(item.getQuantity())
                        .totalPrice(item.getPrice())
                        .build();
            }).toList();

            CompleteOrderView completeOrderView = CompleteOrderView.builder()
                    .date(order.getDate())
                    .status(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .orderItems(orderItemsResponses)
                    .build();

            response.add(PaymentResponseWithOrder.builder()
                    .id(payment.getId())
                    .amountpaid(payment.getAmountpaid())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentDate(payment.getPaymentDate())
                    .orderId(order.getId())
                    .completeOrderViews(List.of(completeOrderView))
                    .build());
        }
        return response;
    }
}
