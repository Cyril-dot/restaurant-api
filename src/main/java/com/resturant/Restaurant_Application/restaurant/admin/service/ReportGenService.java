package com.resturant.Restaurant_Application.restaurant.admin.service;

import com.opencsv.CSVWriter;
import com.resturant.Restaurant_Application.customer.entity.Order;
import com.resturant.Restaurant_Application.customer.entity.OrderItems;
import com.resturant.Restaurant_Application.customer.entity.repo.OrderRepo;
import com.resturant.Restaurant_Application.restaurant.InRestaurantOrders;
import com.resturant.Restaurant_Application.restaurant.InRestaurantPayments;
import com.resturant.Restaurant_Application.restaurant.Toppings;
import com.resturant.Restaurant_Application.restaurant.admin.repo.InRestaurantOrderRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;

import java.io.Writer;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class ReportGenService {

    private final InRestaurantOrderRepo restaurantOrderRepo;
    private final OrderRepo customerOrderRepo;

    /* ===================== FETCH FULL ORDERS TO AVOID LAZY ===================== */
    public List<InRestaurantOrders> fetchAllRestaurantOrders() {
        List<InRestaurantOrders> orders = restaurantOrderRepo.findAll();
        orders.forEach(order -> {
            if (order.getMenu() != null) order.getMenu().getFoodName();
            order.getToppings().size();
            if (order.getPayment() != null) order.getPayment().getAmountPaid();
        });
        return orders;
    }

    public List<Order> fetchAllCustomerOrders() {
        List<Order> orders = customerOrderRepo.findAll();
        orders.forEach(order -> order.getOrderItems().forEach(oi -> {
            oi.getMenu().getFoodName();
            oi.getToppings().size();
        }));
        return orders;
    }

    /* ===================== EXCEL ===================== */
    public Workbook generateSalesExcelReport(List<InRestaurantOrders> orders) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sales Report");

        String[] columns = {"Order ID", "Order Date", "Menu Item", "Toppings", "Quantity", "Price", "Status"};
        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) header.createCell(i).setCellValue(columns[i]);

        int rowNum = 1;
        for (InRestaurantOrders order : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getId());
            row.createCell(1).setCellValue(order.getOrderDate().toString());
            row.createCell(2).setCellValue(order.getMenu().getFoodName());
            row.createCell(3).setCellValue(order.getToppings().stream().map(Toppings::getName).collect(Collectors.joining(", ")));
            row.createCell(4).setCellValue(order.getQuantity());
            row.createCell(5).setCellValue(order.getPrice().doubleValue());
            row.createCell(6).setCellValue(order.getOrderStatus().name());
        }

        for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);
        return workbook;
    }

    /* ===================== CSV ===================== */
    public void writeSalesCsv(List<InRestaurantOrders> orders, Writer writer) {
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            String[] header = {"Order ID", "Order Date", "Menu Item", "Toppings", "Quantity", "Price", "Status"};
            csvWriter.writeNext(header);

            for (InRestaurantOrders order : orders) {
                String[] row = {
                        String.valueOf(order.getId()),
                        order.getOrderDate().toString(),
                        order.getMenu().getFoodName(),
                        order.getToppings().stream().map(Toppings::getName).collect(Collectors.joining(", ")),
                        String.valueOf(order.getQuantity()),
                        order.getPrice().toString(),
                        order.getOrderStatus().name()
                };
                csvWriter.writeNext(row);
            }
            csvWriter.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV", e);
        }
    }

    /* ===================== PDF ===================== */
    public void writeSalesPdf(List<InRestaurantOrders> orders, java.io.OutputStream os) {
        try {
            PdfWriter writer = new PdfWriter(os);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Sales Report"));
            float[] columnWidths = {50, 100, 100, 100, 50, 50, 60};
            Table table = new Table(columnWidths);

            String[] headers = {"Order ID", "Order Date", "Menu Item", "Toppings", "Qty", "Price", "Status"};
            for (String h : headers) table.addCell(h);

            for (InRestaurantOrders order : orders) {
                table.addCell(String.valueOf(order.getId()));
                table.addCell(order.getOrderDate().toString());
                table.addCell(order.getMenu().getFoodName());
                table.addCell(order.getToppings().stream().map(Toppings::getName).collect(Collectors.joining(", ")));
                table.addCell(String.valueOf(order.getQuantity()));
                table.addCell(order.getPrice().toString());
                table.addCell(order.getOrderStatus().name());
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    /* ===================== CUSTOMER + RESTAURANT ORDERS ===================== */
    public List<Map<String, Object>> getAllOrdersReport() {
        List<Map<String, Object>> report = new ArrayList<>();

        // Customer Orders
        for (Order order : fetchAllCustomerOrders()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Order ID", order.getId());
            row.put("Type", "Customer");
            row.put("Customer Name", order.getCustomer().getName());
            row.put("Customer Email", order.getCustomer().getEmail());
            row.put("Date", order.getDate().toLocalDate());
            row.put("Total Amount", order.getTotalAmount());
            row.put("Items", order.getOrderItems().stream()
                    .map(oi -> oi.getMenu().getFoodName() + " x" + oi.getQuantity() +
                            (oi.getToppings().isEmpty() ? "" : " (Toppings: " +
                                    oi.getToppings().stream().map(Toppings::getName).collect(Collectors.joining(", ")) + ")"))
                    .collect(Collectors.joining("; ")));
            row.put("Status", order.getStatus().name());
            report.add(row);
        }

        // Restaurant Orders
        for (InRestaurantOrders order : fetchAllRestaurantOrders()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Order ID", order.getId());
            row.put("Type", "Restaurant");
            row.put("Customer Name", order.getPayment() != null ? order.getPayment().getOrder().getId() : "N/A");
            row.put("Customer Email", "N/A");
            row.put("Date", order.getOrderDate());
            row.put("Total Amount", order.getPrice());
            row.put("Items", order.getMenu().getFoodName() +
                    (order.getToppings().isEmpty() ? "" : " (Toppings: " +
                            order.getToppings().stream().map(Toppings::getName).collect(Collectors.joining(", ")) + ")"));
            row.put("Status", order.getOrderStatus().name());
            report.add(row);
        }

        return report;
    }

    /* ===================== PAYMENT ALLOCATION ===================== */
    public Map<String, Object> getPaymentAllocation(Order order) {
        BigDecimal total = order.getTotalAmount();
        Map<String, Object> allocation = new HashMap<>();
        allocation.put("Total Paid", total);
        allocation.put("Inventory (60%)", total.multiply(BigDecimal.valueOf(0.6)));
        allocation.put("Profit (20%)", total.multiply(BigDecimal.valueOf(0.2)));
        allocation.put("Workers Payment (10%)", total.multiply(BigDecimal.valueOf(0.1)));
        allocation.put("Remaining / Others (10%)", total.multiply(BigDecimal.valueOf(0.1)));
        allocation.put("Payment Date", order.getPayment() != null ? order.getPayment().getPaymentDate() : null);
        return allocation;
    }

    public Map<String, Object> getPaymentAllocation(InRestaurantPayments payment) {
        BigDecimal total = payment.getAmountPaid();
        Map<String, Object> allocation = new HashMap<>();
        allocation.put("Total Paid", total);
        allocation.put("Inventory (60%)", total.multiply(BigDecimal.valueOf(0.6)));
        allocation.put("Profit (20%)", total.multiply(BigDecimal.valueOf(0.2)));
        allocation.put("Workers Payment (10%)", total.multiply(BigDecimal.valueOf(0.1)));
        allocation.put("Remaining / Others (10%)", total.multiply(BigDecimal.valueOf(0.1)));
        allocation.put("Payment Date", payment.getPaymentDate());
        return allocation;
    }

    /* ===================== WEEKLY / MONTHLY SALES ===================== */
    public List<Order> getCustomerOrdersByWeek(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int targetWeek = date.get(weekFields.weekOfWeekBasedYear());
        int targetYear = date.getYear();

        List<Order> weeklyOrders = new ArrayList<>();
        for (Order order : fetchAllCustomerOrders()) {
            LocalDate orderDate = order.getDate().toLocalDate();
            int orderWeek = orderDate.get(weekFields.weekOfWeekBasedYear());
            if (orderWeek == targetWeek && orderDate.getYear() == targetYear) weeklyOrders.add(order);
        }
        return weeklyOrders;
    }

    public List<InRestaurantOrders> getRestaurantOrdersByWeek(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int targetWeek = date.get(weekFields.weekOfWeekBasedYear());
        int targetYear = date.getYear();

        List<InRestaurantOrders> weeklyOrders = new ArrayList<>();
        for (InRestaurantOrders order : fetchAllRestaurantOrders()) {
            LocalDate orderDate = order.getOrderDate().toLocalDate();
            int orderWeek = orderDate.get(weekFields.weekOfWeekBasedYear());
            if (orderWeek == targetWeek && orderDate.getYear() == targetYear) weeklyOrders.add(order);
        }
        return weeklyOrders;
    }


    public List<Map<String, Object>> compareWeeklySales(LocalDate targetDate) {
        List<Map<String, Object>> weeklyTrend = new ArrayList<>();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int targetWeek = targetDate.get(weekFields.weekOfWeekBasedYear());
        int targetYear = targetDate.getYear();

        LocalDate startOfWeek = targetDate.with(weekFields.dayOfWeek(), 1);
        LocalDate endOfWeek = targetDate.with(weekFields.dayOfWeek(), 7);

        for (LocalDate date = startOfWeek; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
            final LocalDate currentDate = date; // FIX: effectively final for lambda

            BigDecimal customerTotal = fetchAllCustomerOrders().stream()
                    .filter(o -> o.getDate().toLocalDate().isEqual(currentDate))
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal restaurantTotal = fetchAllRestaurantOrders().stream()
                    .filter(o -> o.getOrderDate().toLocalDate().isEqual(currentDate))
                    .map(InRestaurantOrders::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", currentDate);
            dayData.put("customerSales", customerTotal);
            dayData.put("restaurantSales", restaurantTotal);
            dayData.put("totalSales", customerTotal.add(restaurantTotal));

            weeklyTrend.add(dayData);
        }

        return weeklyTrend;
    }

    public List<Map<String, Object>> compareMonthlySales(int month, int year) {
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();

        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            final LocalDate currentDate = date; // FIX: effectively final for lambda

            BigDecimal customerTotal = fetchAllCustomerOrders().stream()
                    .filter(o -> o.getDate().getYear() == year && o.getDate().getMonthValue() == month &&
                            o.getDate().toLocalDate().isEqual(currentDate))
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal restaurantTotal = fetchAllRestaurantOrders().stream()
                    .filter(o -> o.getOrderDate().getYear() == year && o.getOrderDate().getMonthValue() == month &&
                            o.getOrderDate().toLocalDate().isEqual(currentDate))
                    .map(InRestaurantOrders::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", currentDate);
            dayData.put("customerSales", customerTotal);
            dayData.put("restaurantSales", restaurantTotal);
            dayData.put("totalSales", customerTotal.add(restaurantTotal));

            monthlyTrend.add(dayData);
        }

        return monthlyTrend;
    }


    // Similarly implement monthly sales, weekly/monthly trends, payment tracking...
}
