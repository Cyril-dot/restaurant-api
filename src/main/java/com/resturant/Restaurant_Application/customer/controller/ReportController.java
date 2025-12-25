package com.resturant.Restaurant_Application.customer.controller;

import com.resturant.Restaurant_Application.customer.entity.Order;
import com.resturant.Restaurant_Application.restaurant.InRestaurantOrders;
import com.resturant.Restaurant_Application.restaurant.InRestaurantPayments;
import com.resturant.Restaurant_Application.restaurant.admin.service.ReportGenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ReportController {

    private final ReportGenService reportGenService;
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    /* ===================== EXCEL REPORT ===================== */
    @GetMapping("/sales/excel")
    public void downloadSalesExcel(HttpServletResponse response) {
        try {
            logger.info("Generating Excel sales report...");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=sales-report.xlsx");

            var orders = reportGenService.fetchAllRestaurantOrders();
            var workbook = reportGenService.generateSalesExcelReport(orders);
            workbook.write(response.getOutputStream());
            workbook.close();

            logger.info("Excel report streamed successfully.");
        } catch (Exception e) {
            logger.error("Error generating Excel report", e);
        }
    }

    /* ===================== CSV REPORT ===================== */
    @GetMapping("/sales/csv")
    public void downloadSalesCsv(HttpServletResponse response) {
        try {
            logger.info("Generating CSV sales report...");
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=sales-report.csv");

            var orders = reportGenService.fetchAllRestaurantOrders();
            reportGenService.writeSalesCsv(orders, new OutputStreamWriter(response.getOutputStream()));

            logger.info("CSV report streamed successfully.");
        } catch (Exception e) {
            logger.error("Error generating CSV report", e);
        }
    }

    /* ===================== PDF REPORT ===================== */
    @GetMapping("/sales/pdf")
    public void downloadSalesPdf(HttpServletResponse response) {
        try {
            logger.info("Generating PDF sales report...");
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=sales-report.pdf");

            var orders = reportGenService.fetchAllRestaurantOrders();
            reportGenService.writeSalesPdf(orders, response.getOutputStream());

            logger.info("PDF report streamed successfully.");
        } catch (Exception e) {
            logger.error("Error generating PDF report", e);
        }
    }

    /* ===================== ALL ORDERS ===================== */
    @GetMapping("/all-orders")
    public List<Map<String, Object>> getAllOrders(@RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate) {
        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : null;
        logger.info("Fetching all orders from {} to {}", start, end);
        return reportGenService.getAllOrdersReport();
    }

    /* ===================== PAYMENT ALLOCATION ===================== */
    @GetMapping("/payment/customer/{orderId}")
    public ResponseEntity<?> getCustomerPayment(@PathVariable Integer orderId) {
        List<Order> weeklyOrders = reportGenService.getCustomerOrdersByWeek(LocalDate.now());
        Order order = weeklyOrders.stream().filter(o -> o.getId().equals(orderId)).findFirst().orElse(null);

        if (order == null) {
            logger.warn("Customer order {} not found", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Customer order not found"));
        }

        return ResponseEntity.ok(reportGenService.getPaymentAllocation(order));
    }

    @GetMapping("/payment/restaurant/{paymentId}")
    public ResponseEntity<?> getRestaurantPayment(@PathVariable Integer paymentId) {
        List<InRestaurantOrders> weeklyOrders = reportGenService.getRestaurantOrdersByWeek(LocalDate.now());
        InRestaurantPayments payment = weeklyOrders.stream()
                .map(InRestaurantOrders::getPayment)
                .filter(p -> p != null && p.getId().equals(paymentId))
                .findFirst()
                .orElse(null);

        if (payment == null) {
            logger.warn("Restaurant payment {} not found", paymentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Restaurant payment not found"));
        }

        return ResponseEntity.ok(reportGenService.getPaymentAllocation(payment));
    }

    /* ===================== WEEKLY SALES ===================== */
    @GetMapping("/sales/weekly")
    public Map<String, Object> getWeeklySales(@RequestParam(required = false) String date) {
        LocalDate targetDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();

        // Customer Orders DTOs
        List<Map<String, Object>> customerOrders = reportGenService.getCustomerOrdersByWeek(targetDate)
                .stream()
                .map(order -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId", order.getId());
                    map.put("customerName", order.getCustomer().getName());
                    map.put("date", order.getDate().toLocalDate());
                    map.put("totalAmount", order.getTotalAmount());
                    map.put("items", order.getOrderItems().stream()
                            .map(oi -> oi.getMenu().getFoodName() + " x" + oi.getQuantity() +
                                    (oi.getToppings().isEmpty() ? "" : " (Toppings: " +
                                            oi.getToppings().stream().map(t -> t.getName()).collect(Collectors.joining(", ")) + ")"))
                            .collect(Collectors.joining("; ")));
                    map.put("status", order.getStatus().name());
                    return map;
                })
                .collect(Collectors.toList());

        // Restaurant Orders DTOs
        List<Map<String, Object>> restaurantOrders = reportGenService.getRestaurantOrdersByWeek(targetDate)
                .stream()
                .map(order -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId", order.getId());
                    map.put("date", order.getOrderDate());
                    map.put("menuItem", order.getMenu().getFoodName());
                    map.put("toppings", order.getToppings().stream().map(t -> t.getName()).collect(Collectors.joining(", ")));
                    map.put("quantity", order.getQuantity());
                    map.put("totalAmount", order.getPrice());
                    map.put("status", order.getOrderStatus().name());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("customerOrders", customerOrders);
        response.put("restaurantOrders", restaurantOrders);

        return response;
    }

    /* ===================== MONTHLY SALES ===================== */
    @GetMapping("/sales/monthly")
    public Map<String, Object> getMonthlySales(@RequestParam int month, @RequestParam int year) {
        // Customer Orders DTOs
        List<Map<String, Object>> customerOrders = reportGenService.fetchAllCustomerOrders()
                .stream()
                .filter(o -> o.getDate().getMonthValue() == month && o.getDate().getYear() == year)
                .map(order -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId", order.getId());
                    map.put("customerName", order.getCustomer().getName());
                    map.put("date", order.getDate().toLocalDate());
                    map.put("totalAmount", order.getTotalAmount());
                    map.put("items", order.getOrderItems().stream()
                            .map(oi -> oi.getMenu().getFoodName() + " x" + oi.getQuantity() +
                                    (oi.getToppings().isEmpty() ? "" : " (Toppings: " +
                                            oi.getToppings().stream().map(t -> t.getName()).collect(Collectors.joining(", ")) + ")"))
                            .collect(Collectors.joining("; ")));
                    map.put("status", order.getStatus().name());
                    return map;
                })
                .collect(Collectors.toList());

        // Restaurant Orders DTOs
        List<Map<String, Object>> restaurantOrders = reportGenService.fetchAllRestaurantOrders()
                .stream()
                .filter(o -> o.getOrderDate().getMonthValue() == month && o.getOrderDate().getYear() == year)
                .map(order -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId", order.getId());
                    map.put("date", order.getOrderDate());
                    map.put("menuItem", order.getMenu().getFoodName());
                    map.put("toppings", order.getToppings().stream().map(t -> t.getName()).collect(Collectors.joining(", ")));
                    map.put("quantity", order.getQuantity());
                    map.put("totalAmount", order.getPrice());
                    map.put("status", order.getOrderStatus().name());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("customerOrders", customerOrders);
        response.put("restaurantOrders", restaurantOrders);

        return response;
    }

    /* ===================== WEEKLY / MONTHLY TRENDS ===================== */
    @GetMapping("/sales/trend/weekly")
    public Map<String, Object> weeklyTrend(@RequestParam(required = false) String date) {
        LocalDate targetDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        // Implement trend calculation in service
        Map<String, Object> trendData = new HashMap<>();
        trendData.put("weeklyTrend", reportGenService.compareWeeklySales(targetDate)); // returns List<Map<String,Object>>
        return trendData;
    }

    @GetMapping("/sales/trend/monthly")
    public Map<String, Object> monthlyTrend(@RequestParam int month, @RequestParam int year) {
        // Implement trend calculation in service
        Map<String, Object> trendData = new HashMap<>();
        trendData.put("monthlyTrend", reportGenService.compareMonthlySales(month, year)); // returns List<Map<String,Object>>
        return trendData;
    }

}
