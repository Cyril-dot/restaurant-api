package com.resturant.Restaurant_Application.restaurant.admin;

import com.resturant.Restaurant_Application.ExceptionHandlers.UserDoesNotExistException;
import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.customer.entity.Enum.Role;
import com.resturant.Restaurant_Application.customer.entity.dtos.*;
import com.resturant.Restaurant_Application.customer.security.TokenService;
import com.resturant.Restaurant_Application.customer.service.CustomerCreationService;
import com.resturant.Restaurant_Application.restaurant.admin.dtos.AdminRequest;
import com.resturant.Restaurant_Application.restaurant.admin.dtos.AdminResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;
    private final TokenService tokenService;

    // --------- Extract Admin from JWT ---------
    private AdminEntity getAdminFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String email = tokenService.getEmailFromAccessToken(token);
        return service.getUserByEmail(email);
    }

    // --------- REGISTER ADMIN (PUBLIC) ---------
    @PostMapping("/register")
    public ResponseEntity<AdminResponse> registerAdmin(
            @RequestBody AdminRequest registerRequest) {

        AdminResponse response = service.createAdmin(registerRequest);
        return ResponseEntity.ok(response);
    }

    // --------- LOGIN ADMIN (PUBLIC) ---------
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        LoginResponse response = service.adminLogin(request);
        return ResponseEntity.ok(response);
    }

    // --------- UPDATE PASSWORD (PROTECTED) ---------
    @PutMapping("/update/password")
    public ResponseEntity<PasswordUpdateResponse> updatePassword(
            @RequestBody PasswordUpdateRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        AdminEntity admin = getAdminFromToken(authHeader);

        PasswordUpdateResponse response =
                service.passwordUpdate(request, admin.getEmail());

        return ResponseEntity.ok(response);
    }

    // --------- ROLE-BASED ENDPOINT ---------
    @GetMapping("/welcome")
    public ResponseEntity<String> welcomeAdmin(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        AdminEntity admin = getAdminFromToken(authHeader);

        if (admin.getRole() == Role.ADMIN) {
            return ResponseEntity.ok("Welcome Admin!");
        }

        return ResponseEntity.status(403).body("Access denied");
    }
}
