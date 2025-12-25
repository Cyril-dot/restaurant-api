package com.resturant.Restaurant_Application.customer.service;

import com.resturant.Restaurant_Application.ExceptionHandlers.UserDoesNotExistException;
import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.customer.entity.Enum.Status;
import com.resturant.Restaurant_Application.customer.entity.Order;
import com.resturant.Restaurant_Application.customer.entity.OrderItems;
import com.resturant.Restaurant_Application.customer.entity.PaymentEntity;
import com.resturant.Restaurant_Application.customer.entity.dtos.*;
import com.resturant.Restaurant_Application.customer.entity.repo.CustomerRepo;
import com.resturant.Restaurant_Application.customer.entity.repo.OrderItemsRepo;
import com.resturant.Restaurant_Application.customer.entity.repo.OrderRepo;
import com.resturant.Restaurant_Application.customer.entity.repo.PaymentRepo;
import com.resturant.Restaurant_Application.restaurant.Menu;
import com.resturant.Restaurant_Application.restaurant.Toppings;
import com.resturant.Restaurant_Application.restaurant.admin.repo.MenuRepo;
import com.resturant.Restaurant_Application.restaurant.admin.repo.ToppingsRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantOrderService {

    private final OrderItemsRepo orderItemsRepo;
    private final MenuRepo menuRepo;
    private final ToppingsRepo toppingsRepo;
    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;
    private final CustomerCreationService service;


    // to view all menu items
    public List<MenuResponse> viewMenu(String email) {
        // to check if the customer exists
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) {
            throw new UsernameNotFoundException("Customer does not exist");
        }

        List<Menu> allMenu = menuRepo.findAll();

        if (allMenu.isEmpty()) {
            throw new UsernameNotFoundException("No dishes uploaded yet");
        }

        return mapToMenuList(allMenu);
    }

    // search through menu
    public List<MenuResponse> searchMenuItems(String email, String keyword) {
        // 1️⃣ Verify customer exists
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) {
            throw new UserDoesNotExistException("Customer does not exist");
        }

        // 2️⃣ Search menu items
        List<Menu> menuList = menuRepo.searchMenu(keyword);

        // 3️⃣ Check if search returned anything
        if (menuList.isEmpty()) {
            throw new UserDoesNotExistException("No menu items found matching: " + keyword);
        }

        // 4️⃣ Map to MenuResponse
        List<MenuResponse> response = menuList.stream()
                .map(menu -> MenuResponse.builder()
                        .id(menu.getId())
                        .foodName(menu.getFoodName())
                        .category(menu.getCategory())
                        .is_available(menu.getIs_available())
                        .price(menu.getPrice())
                        .description(menu.getDescription())
                        .build())
                .toList();

        return response;
    }


    private List<MenuResponse> mapToMenuList(List<Menu> menus) {
        return menus.stream()
                .map(menu -> MenuResponse.builder()
                        .id(menu.getId())
                        .foodName(menu.getFoodName())
                        .price(menu.getPrice())
                        .category(menu.getCategory())
                        .description(menu.getDescription())
                        .is_available(menu.getIs_available())
                        .build())
                .toList();
    }

    // to group according to category
    public List<MenuResponse> groupByCategory(String email, String category) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) {
            throw new UsernameNotFoundException("Customer does not exist");
        }

        List<Menu> menuList = menuRepo.findAllByCategoryLike(category);
        if (menuList.isEmpty()) {
            throw new UserDoesNotExistException("No menu items found within category: " + category);
        }

        return mapToMenuList(menuList);
    }

    // to view all toppings
    public List<ToppingsResponse> viewToppings(String email) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) {
            throw new UsernameNotFoundException("Customer does not exist");
        }

        List<Toppings> response = toppingsRepo.findAll();
        if (response.isEmpty()) {
            throw new UsernameNotFoundException("No toppings uploaded yet");
        }

        return mapToToppings(response);
    }

    public List<ToppingsResponse> searchToppings(String email, String keyword){
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) {
            throw new UsernameNotFoundException("Customer does not exist");
        }

        List<Toppings> response = toppingsRepo.searchToppings(keyword);
        if (response.isEmpty()) {
            throw new UsernameNotFoundException("No toppings found matching: " + keyword);
        }

        return mapToToppings(response);
    }

    private List<ToppingsResponse> mapToToppings(List<Toppings> toppings) {
        return toppings.stream().map(
                        toppings1 -> ToppingsResponse.builder()
                                .id(toppings1.getId())
                                .name(toppings1.getName())
                                .price(toppings1.getPrice())
                                .build())
                .toList();
    }


    // to view all orders that is customer orders pls
    public List<OrderResponse> viewOrders(String email) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) {
            throw new UserDoesNotExistException("Customer does not exist");
        }

        // to and print out only orders of the customer
        List<Order> customerOrder = orderRepo.findByCustomer_Id(customer.getId());
        if (customerOrder.isEmpty()) {
            throw new UserDoesNotExistException("No dishes and orders placed yet");
        }

        return mapToCustomerOrders(customerOrder);

    }

    private List<OrderResponse> mapToCustomerOrders(List<Order> order) {
        return order.stream().map(
                        customerOrder -> OrderResponse.builder()
                                .id(customerOrder.getId())
                                .date(customerOrder.getDate())
                                .status(customerOrder.getStatus())
                                .totalAmount(customerOrder.getTotalAmount())
                                .build())
                .toList();
    }

    // to group orders and order items
    // here the order items i going to be using the ids of the specific menu items t fetch the data
    // here the order items would be mapped to the order id

    public List<CompleteOrderView> getCompleteOrders(String email) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) {
            throw new UserDoesNotExistException("Customer does not exist");
        }

        List<Order> customerOrders = orderRepo.findByCustomer_Id(customer.getId());
        if (customerOrders.isEmpty()) {
            throw new UserDoesNotExistException("No orders placed yet");
        }

        List<CompleteOrderView> completeOrders = mapOrdersToCompleteOrderView(customerOrders);

        // Print for debugging
        completeOrders.forEach(order -> {
            System.out.println("Order Date: " + order.getDate());
            System.out.println("Status: " + order.getStatus());
            System.out.println("Total Amount: " + order.getTotalAmount());
            System.out.println("Items:");
            order.getOrderItems().forEach(item -> {
                System.out.println("  Menu: " + item.getMenuItem().getFoodName());
                System.out.println("  Category: " + item.getMenuItem().getCategory());
                System.out.println("  Quantity: " + item.getQuantity());
                System.out.println("  Item Total: " + item.getTotalPrice());
                if (!item.getToppings().isEmpty()) {
                    System.out.println("  Toppings: " + item.getToppings().stream()
                            .map(t -> t.getName() + "($" + t.getPrice() + ")")
                            .collect(Collectors.joining(", ")));
                }
                System.out.println();
            });
            System.out.println("-------------------------------");
        });

        return completeOrders;
    }

    private List<CompleteOrderView> mapOrdersToCompleteOrderView(List<Order> orders) {
        List<CompleteOrderView> completeOrders = new ArrayList<>();

        for (Order order : orders) {
            List<OrderItemsResponse> orderItems = order.getOrderItems().stream().map(item -> {

                // Map toppings
                List<ToppingsResponse> toppings = item.getToppings().stream()
                        .map(t -> new ToppingsResponse(t.getId(), t.getName(), t.getPrice()))
                        .collect(Collectors.toList());

                // Map menu item
                MenuResponse menuResponse = new MenuResponse(
                        item.getMenu().getId(),
                        item.getMenu().getFoodName(),
                        item.getMenu().getCategory(),
                        item.getMenu().getIs_available(),
                        item.getMenu().getPrice(),
                        item.getMenu().getDescription()
                );

                // Use stored total from OrderItems
                BigDecimal itemTotal = item.getPrice(); // ✅ already calculated

                return OrderItemsResponse.builder()
                        .menuItem(menuResponse)
                        .toppings(toppings)
                        .quantity(item.getQuantity())
                        .totalPrice(itemTotal) // ✅ no need to recalc
                        .build();
            }).collect(Collectors.toList());

            CompleteOrderView completeOrder = CompleteOrderView.builder()
                    .date(order.getDate())
                    .status(order.getStatus())
                    .totalAmount(order.getTotalAmount()) // from Order entity
                    .orderItems(orderItems)
                    .build();

            completeOrders.add(completeOrder);
        }

        return completeOrders;
    }


    // to fetch order by id
    // ------------------- Fetch order by id -------------------
    public CompleteOrderView getOrderById(String email, Integer orderId) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UserDoesNotExistException("Customer does not exist");

        Order order = orderRepo.findByIdAndCustomerId(orderId, customer.getId())
                .orElseThrow(() -> new UserDoesNotExistException("Order not found"));

        return mapOrdersToCompleteOrderView(List.of(order)).get(0);
    }

    // to view mst recent orders
    public List<CompleteOrderView> getMostRecentOrders(String email) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UserDoesNotExistException("Customer does not exist");

        List<Order> orders = orderRepo.findByCustomerIdOrderByDateDesc(customer.getId());
        if (orders.isEmpty()) throw new UserDoesNotExistException("No recent orders");

        return mapOrdersToCompleteOrderView(orders);
    }

    // ------------------- Fetch orders sorted by date -------------------
    public List<CompleteOrderView> getOrdersByDate(String email) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UserDoesNotExistException("Customer does not exist");

        List<Order> orders = orderRepo.findByCustomerIdOrderByDateDesc(customer.getId());
        if (orders.isEmpty()) throw new UserDoesNotExistException("No orders placed yet");

        return mapOrdersToCompleteOrderView(orders);
    }

    // find orders by date
    public List<CompleteOrderView> getByOrderDate(String email, LocalDate date){
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UserDoesNotExistException("Customer does not exist");

        List<Order> orders = orderRepo.findByCustomerIdAndOrderDate(customer.getId(), date);
        if (orders.isEmpty()) throw new UserDoesNotExistException("No orders placed yet");

        return mapOrdersToCompleteOrderView(orders);

    }

    // ------------------- Fetch orders by status -------------------
    public List<CompleteOrderView> getOrdersByStatus(String email, Status status) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UserDoesNotExistException("Customer does not exist");

        List<Order> orders = orderRepo.findByCustomerIdAndStatus(customer.getId(), status.name().toUpperCase());
        if (orders.isEmpty()) throw new UserDoesNotExistException("No orders with status: " + status);

        return mapOrdersToCompleteOrderView(orders);
    }


    // to view order items
    public List<OrderItemsResponse> getOrderItems(String email) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UserDoesNotExistException("Customer does not exist");

        //now we are going to be viewing all customers order items
        List<OrderItems> response = orderItemsRepo.findAll();
        if (response.isEmpty()) throw new UserDoesNotExistException("No order items found");

        return mapToOrderItemsResponse(response);
    }

    private List<OrderItemsResponse> mapToOrderItemsResponse(List<OrderItems> orderItemsList) {
        List<OrderItemsResponse> orderItemsResponse = new ArrayList<>();

        for (OrderItems item : orderItemsList) {
            // Map Menu info
            MenuResponse menuResponse = new MenuResponse(
                    item.getMenu().getId(),
                    item.getMenu().getFoodName(),
                    item.getMenu().getCategory(),
                    item.getMenu().getIs_available(),
                    item.getMenu().getPrice(),
                    item.getMenu().getDescription()
            );

            // Map Toppings
            List<ToppingsResponse> toppings = new ArrayList<>();
            if (item.getToppings() != null) {
                toppings = item.getToppings().stream()
                        .map(t -> new ToppingsResponse(t.getId(), t.getName(), t.getPrice()))
                        .toList();
            }

            // Build DTO using stored price
            OrderItemsResponse response = OrderItemsResponse.builder()
                    .menuItem(menuResponse)
                    .toppings(toppings)
                    .quantity(item.getQuantity())
                    .totalPrice(item.getPrice()) // stored in DB
                    .build();

            orderItemsResponse.add(response);
        }

        return orderItemsResponse;
    }

    // get order items by order id
    public List<OrderItemsResponse> getOrderItemsByOrderId(String email, Integer orderId) {
        // Get customer
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UserDoesNotExistException("Customer does not exist");

        List<OrderItems> orderItems = orderItemsRepo.findByOrderId(orderId);

        if (orderItems.isEmpty()) {
            throw new UserDoesNotExistException("No items found for this order");
        }

        // Map the order items to DTO
        return mapToOrderItemsResponse(orderItems);
    }


    // view all payment and keep track
    public List<PaymentResponseWithOrder> viewPaymentRecords(String email) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UserDoesNotExistException("Customer does not exist");

        List<PaymentEntity> response = paymentRepo.findByCustomerId(customer.getId());
        if (response.isEmpty()) throw new UserDoesNotExistException("No payments found");

        return mapToCustomerPayments(response);
    }

    private List<PaymentResponseWithOrder> mapToCustomerPayments(List<PaymentEntity> paymentEntities) {

        List<PaymentResponseWithOrder> response = new ArrayList<>();

        for (PaymentEntity payment : paymentEntities) {

            Order order = payment.getOrder();

            // 1. Map order items
            List<OrderItemsResponse> orderItemsResponses = new ArrayList<>();

            for (OrderItems item : order.getOrderItems()) {

                MenuResponse menuResponse = MenuResponse.builder()
                        .id(item.getMenu().getId())
                        .foodName(item.getMenu().getFoodName())
                        .category(item.getMenu().getCategory())
                        .is_available(item.getMenu().getIs_available())
                        .price(item.getMenu().getPrice())
                        .description(item.getMenu().getDescription())
                        .build();

                List<ToppingsResponse> toppingsResponses = new ArrayList<>();
                if (item.getToppings() != null) {
                    toppingsResponses = item.getToppings().stream()
                            .map(t -> ToppingsResponse.builder()
                                    .id(t.getId())
                                    .name(t.getName())
                                    .price(t.getPrice())
                                    .build())
                            .toList();
                }

                orderItemsResponses.add(
                        OrderItemsResponse.builder()
                                .menuItem(menuResponse)
                                .toppings(toppingsResponses)
                                .quantity(item.getQuantity())
                                .totalPrice(item.getPrice())
                                .build()
                );
            }

            // 2. Build CompleteOrderView
            CompleteOrderView completeOrderView = CompleteOrderView.builder()
                    .date(order.getDate())
                    .status(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .orderItems(orderItemsResponses)
                    .build();

            // 3. Build PaymentResponseWithOrder
            PaymentResponseWithOrder paymentResponse = PaymentResponseWithOrder.builder()
                    .id(payment.getId())
                    .amountpaid(payment.getAmountpaid())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentDate(payment.getPaymentDate())
                    .orderId(order.getId())
                    .completeOrderViews(List.of(completeOrderView))
                    .build();

            response.add(paymentResponse);
        }

        return response;
    }


    // view payments by date
    public List<PaymentResponseWithOrder> getPaymentsByDate(String email, LocalDate date) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UserDoesNotExistException("Customer does not exist");

        List<PaymentEntity> response = paymentRepo.findByPaymentDate(date);
        if (response == null) throw new UserDoesNotExistException("No payments found");
        return mapToCustomerPayments(response);
    }

    // to view mst recent payments
    public List<PaymentResponseWithOrder> getMostRecentPayments(String email) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UserDoesNotExistException("Customer does not exist");

        List<PaymentEntity> response = paymentRepo.findTop5MostRecentPayments();
        if (response == null) throw new UserDoesNotExistException("No payments found");

        return mapToCustomerPayments(response);
    }

    // ------------------- Fetch payments by order id -------------------
    public PaymentResponseWithOrder getByOrderId_payments(String email, Integer orderId) {
        CustomerEntity customer = service.getUserByEmail(email);
        if (customer == null) throw new UserDoesNotExistException("Customer does not exist");

        PaymentEntity payment = paymentRepo.findByOrderId(orderId);
        if (payment == null)  throw new UserDoesNotExistException("No payments found");

        return mapToCustomerPayments(List.of(payment)).get(0);
    }


    // view most recent payments


}

