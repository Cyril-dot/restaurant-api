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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer")
public class RegistrationController {
    private final CustomerCreationService service;
    private final TokenService tokenService;

    private CustomerEntity getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String email = tokenService.getEmailFromAccessToken(token);
        return service.getUserByEmail(email);
    }

    @PostMapping("/register")
    public ResponseEntity<?> addCustomer(@RequestBody CustomerCreationRequest request){
        try {
            CustomerCreationResponse response = service.createCustomer(request);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        try {
            LoginResponse response = service.customerLogin(request);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest request, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader){
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

    @PutMapping("/update/details")
    public ResponseEntity<?> userUpdate(CustomerUpdateRequest request, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader){
        CustomerEntity user = getUserFromToken(authHeader);
        try {
            CustomerUpdateResponse response = service.customerUpdate(request, user.getEmail());
            return ResponseEntity.ok(response);
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.badRequest().build();
        }  catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }



}
