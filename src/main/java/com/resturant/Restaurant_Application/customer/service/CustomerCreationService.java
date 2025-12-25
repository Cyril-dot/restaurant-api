package com.resturant.Restaurant_Application.customer.service;

import com.resturant.Restaurant_Application.ExceptionHandlers.UserDoesNotExistException;
import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.customer.entity.dtos.*;
import com.resturant.Restaurant_Application.customer.entity.repo.CustomerRepo;
import com.resturant.Restaurant_Application.customer.security.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerCreationService {

    private final CustomerRepo customerRepo;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final NotificationService notificationService; // Injected service

    // ----------------- CREATE CUSTOMER -----------------
    public CustomerCreationResponse createCustomer(CustomerCreationRequest request) {
        CustomerEntity customer = new CustomerEntity();
        customer.setName(request.getName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));

        customerRepo.save(customer);

        log.info("Customer created successfully: {}", customer.getEmail());

        // Send notification to admin
        notificationService.createNotification(
                "New Customer Registered",
                "Customer " + customer.getName() + " (" + customer.getEmail() + ") has registered."
        );

        return mapToCustomerResponse(customer);
    }

    private CustomerCreationResponse mapToCustomerResponse(CustomerEntity customer) {
        return CustomerCreationResponse.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerPhoneNumber(customer.getPhoneNumber())
                .customerEmail(customer.getEmail())
                .build();
    }

    // ----------------- VERIFY PASSWORD -----------------
    public boolean isPasswordValid(CustomerEntity user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // ----------------- GET CUSTOMER -----------------
    public CustomerEntity getUserById(Integer id) {
        return customerRepo.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException("Customer not found"));
    }

    public CustomerEntity getUserByEmail(String email) {
        return customerRepo.findByEmail(email)
                .orElseThrow(() -> new UserDoesNotExistException("Customer not found"));
    }

    // ----------------- CUSTOMER LOGIN -----------------
    public LoginResponse customerLogin(LoginRequest loginRequest) {

        if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()
                || loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            log.warn("Login failed: email or password not provided");
            throw new UserDoesNotExistException("Please enter your credentials");
        }

        CustomerEntity customer = getUserByEmail(loginRequest.getEmail());

        if (!isPasswordValid(customer, loginRequest.getPassword())) {
            log.warn("Login failed: incorrect password for user {}", loginRequest.getEmail());
            throw new UserDoesNotExistException("Password doesn't match");
        }

        String accessToken = tokenService.generateAccessToken(customer);
        String refreshToken = tokenService.generateRefreshToken(customer).getToken();

        log.info("Customer logged in successfully: {}", customer.getEmail());

        // Send notification to admin
        notificationService.createNotification(
                "Customer Logged In",
                "Customer " + customer.getName() + " (" + customer.getEmail() + ") has logged in."
        );

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // ----------------- UPDATE PASSWORD -----------------
    public PasswordUpdateResponse passwordUpdate(PasswordUpdateRequest request, String email) {

        CustomerEntity customer = getUserByEmail(email);

        if (!isPasswordValid(customer, request.getOldPassword())) {
            log.warn("Password update failed: incorrect old password for user {}", email);
            throw new UserDoesNotExistException("Old password doesn't match");
        }

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepo.save(customer);

        log.info("Password updated successfully for user {}", email);

        // Send notification to admin
        notificationService.createNotification(
                "Customer Password Updated",
                "Customer " + customer.getName() + " (" + customer.getEmail() + ") updated their password."
        );

        return PasswordUpdateResponse.builder()
                .message("Password updated successfully")
                .build();
    }

    // ----------------- UPDATE CUSTOMER DETAILS -----------------
    public CustomerUpdateResponse customerUpdate(CustomerUpdateRequest request, String email) {

        CustomerEntity customer = getUserByEmail(email);

        // Update only non-null and non-blank fields
        customer.setName((request.getName() != null && !request.getName().isBlank())
                ? request.getName() : customer.getName());

        customer.setPhoneNumber((request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank())
                ? request.getPhoneNumber() : customer.getPhoneNumber());

        customer.setEmail((request.getEmail() != null && !request.getEmail().isBlank())
                ? request.getEmail() : customer.getEmail());

        customerRepo.save(customer);

        log.info("Customer updated successfully: {}", customer.getEmail());

        // Send notification to admin
        notificationService.createNotification(
                "Customer Details Updated",
                "Customer " + customer.getName() + " (" + customer.getEmail() + ") updated their details."
        );

        return CustomerUpdateResponse.builder()
                .name(customer.getName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .message("Customer updated successfully")
                .build();
    }
}
