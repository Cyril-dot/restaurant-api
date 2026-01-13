package com.resturant.Restaurant_Application.customer.controller;

import com.resturant.Restaurant_Application.ExceptionHandlers.UserDoesNotExistException;
import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.customer.entity.dtos.*;
import com.resturant.Restaurant_Application.customer.security.TokenService;
import com.resturant.Restaurant_Application.customer.service.CustomerCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer")
public class RegistrationController {

    private final CustomerCreationService service;
    private final TokenService tokenService;

    // -------------------------
    // Helper: extract user from token
    // -------------------------
    private CustomerEntity getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String email = tokenService.getEmailFromAccessToken(token);
        return service.getUserByEmail(email);
    }

    // -------------------------
    // Customer registration
    // -------------------------
    @PostMapping("/register")
    public ResponseEntity<?> addCustomer(@RequestBody CustomerCreationRequest request){
        try {
            CustomerCreationResponse response = service.createCustomer(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating customer: " + e.getMessage());
        }
    }

    // -------------------------
    // Customer login
    // -------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        try {
            LoginResponse response = service.customerLogin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e){
            return ResponseEntity.badRequest().body("Invalid login credentials");
        }
    }

    // -------------------------
    // Update password
    // -------------------------
    @PutMapping("/update/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest request,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader){
        CustomerEntity user = getUserFromToken(authHeader);
        try {
            PasswordUpdateResponse response = service.passwordUpdate(request, user.getPassword());
            return ResponseEntity.ok(response);
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.badRequest().body("User does not exist");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating password");
        }
    }

    // -------------------------
    // Update customer details
    // -------------------------
    @PutMapping("/update/details")
    public ResponseEntity<?> userUpdate(@RequestBody CustomerUpdateRequest request,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader){
        CustomerEntity user = getUserFromToken(authHeader);
        try {
            CustomerUpdateResponse response = service.customerUpdate(request, user.getEmail());
            return ResponseEntity.ok(response);
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.badRequest().body("User does not exist");
        }  catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating details");
        }
    }

    // -------------------------
    // CUSTOMER: view own details (/me)
    // -------------------------
    @GetMapping("/me")
    public ResponseEntity<?> getMyDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader){
        try {
            CustomerEntity user = getUserFromToken(authHeader);
            return ResponseEntity.ok(user);
        } catch (Exception e){
            return ResponseEntity.badRequest().body("Error fetching customer details");
        }
    }

    // -------------------------
    // ADMIN: view customer by ID
    // -------------------------
    @GetMapping("/details/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCustomerById(@PathVariable Integer id){
        try {
            CustomerEntity customer = service.getUserById(id);
            return ResponseEntity.ok(customer);
        } catch (UserDoesNotExistException e){
            return ResponseEntity.badRequest().body("Customer with ID " + id + " does not exist");
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("Error fetching customer details");
        }
    }
}
