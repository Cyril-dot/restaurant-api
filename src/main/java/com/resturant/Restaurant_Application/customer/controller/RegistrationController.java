package com.resturant.Restaurant_Application.customer.controller;

import com.resturant.Restaurant_Application.ExceptionHandlers.UserDoesNotExistException;
import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.customer.entity.dtos.*;
import com.resturant.Restaurant_Application.customer.security.TokenService;
import com.resturant.Restaurant_Application.customer.service.CustomerCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer")
public class RegistrationController {
    private final CustomerCreationService service;
    private final TokenService tokenService;

    // Get customer from token
    private CustomerEntity getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String email = tokenService.getEmailFromAccessToken(token);
        return service.getUserByEmail(email);
    }

    // -------------------
    // Register Customer
    // -------------------
    @PostMapping("/register")
    public ResponseEntity<?> addCustomer(@RequestBody CustomerCreationRequest request){
        try {
            CustomerCreationResponse response = service.createCustomer(request);
            return ResponseEntity.ok(response);
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    // -------------------
    // Customer Login
    // -------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        try {
            LoginResponse response = service.customerLogin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    // -------------------
    // Update Password
    // -------------------
    @PutMapping("/update/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest request,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader){
        CustomerEntity user = getUserFromToken(authHeader);
        try {
            PasswordUpdateResponse response = service.passwordUpdate(request, user.getPassword());
            return ResponseEntity.ok(response);
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // -------------------
    // Update Customer Details
    // -------------------
    @PutMapping("/update/details")
    public ResponseEntity<?> userUpdate(@RequestBody CustomerUpdateRequest request,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader){
        CustomerEntity user = getUserFromToken(authHeader);
        try {
            CustomerUpdateResponse response = service.customerUpdate(request, user.getEmail());
            return ResponseEntity.ok(response);
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // -------------------
    // Get Customer Details by Token (/me)
    // -------------------
    @GetMapping("/me")
    public ResponseEntity<?> getMyDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader){
        CustomerEntity user = getUserFromToken(authHeader);

        // Map to DTO / record
        CustomerResponseDTO dto = new CustomerResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole()
        );

        return ResponseEntity.ok(dto);
    }

    // -------------------
    // Get Customer Details by ID (Admin)
    // -------------------
    @GetMapping("/details/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Integer id){
        try {
            CustomerEntity user = service.getUserById(id);

            CustomerResponseDTO dto = new CustomerResponseDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getRole()
            );

            return ResponseEntity.ok(dto);

        } catch (UserDoesNotExistException e) {
            return ResponseEntity.badRequest().body("Customer not found");
        }
    }
}
