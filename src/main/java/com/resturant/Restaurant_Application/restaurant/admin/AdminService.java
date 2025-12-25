package com.resturant.Restaurant_Application.restaurant.admin;

import com.resturant.Restaurant_Application.ExceptionHandlers.UserDoesNotExistException;
import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.customer.entity.dtos.LoginRequest;
import com.resturant.Restaurant_Application.customer.entity.dtos.LoginResponse;
import com.resturant.Restaurant_Application.customer.entity.dtos.PasswordUpdateRequest;
import com.resturant.Restaurant_Application.customer.entity.dtos.PasswordUpdateResponse;
import com.resturant.Restaurant_Application.customer.security.TokenService;
import com.resturant.Restaurant_Application.restaurant.admin.dtos.AdminRequest;
import com.resturant.Restaurant_Application.restaurant.admin.dtos.AdminResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final PasswordEncoder passwordEncoder;
    private final AdminRepo adminRepo;
    private final TokenService tokenService;

    // ----------------- VERIFY PASSWORD -----------------
    public boolean isPasswordValid(AdminEntity user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // ----------------- GET CUSTOMER -----------------
    public AdminEntity getUserById(Integer id) {
        return adminRepo.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException("Customer not found"));
    }

    public AdminEntity getUserByEmail(String email) {
        return adminRepo.findByEmail(email)
                .orElseThrow(() -> new UserDoesNotExistException("Customer not found"));
    }

    public AdminResponse createAdmin(AdminRequest adminRequest) {

        // Check if email already exists
        if (adminRepo.findByEmail(adminRequest.getEmail()).isPresent()) {
            throw new UserDoesNotExistException("Email is already in use");
        }

        AdminEntity adminEntity = new AdminEntity();
        adminEntity.setName(adminRequest.getName());
        adminEntity.setEmail(adminRequest.getEmail());
        adminEntity.setPhoneNumber(adminRequest.getPhoneNumber());
        adminEntity.setPassword(passwordEncoder.encode(adminRequest.getPassword()));

        adminRepo.save(adminEntity);

        return mapToAdmin(adminEntity);
    }


    private AdminResponse mapToAdmin(AdminEntity adminEntity) {
        return AdminResponse.builder()
                .id(adminEntity.getId())
                .name(adminEntity.getName())
                .email(adminEntity.getEmail())
                .phoneNumber(adminEntity.getPhoneNumber())
                .build();
    }

    public PasswordUpdateResponse passwordUpdate(PasswordUpdateRequest request, String email) {

        AdminEntity customer = getUserByEmail(email);

        if (!isPasswordValid(customer, request.getOldPassword())) {
            log.warn("Password update failed: incorrect old password for user {}", email);
            throw new UserDoesNotExistException("Old password doesn't match");
        }

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminRepo.save(customer);

        log.info("Password updated successfully for user {}", email);

        return PasswordUpdateResponse.builder()
                .message("Password updated successfully")
                .build();
    }

    // ----------------- admin LOGIN -----------------
    public LoginResponse adminLogin(LoginRequest loginRequest) {

        if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()
                || loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            log.warn("Login failed: email or password not provided");
            throw new UserDoesNotExistException("Please enter your credentials");
        }

        AdminEntity customer = getUserByEmail(loginRequest.getEmail());

        if (!isPasswordValid(customer, loginRequest.getPassword())) {
            log.warn("Login failed: incorrect password for user {}", loginRequest.getEmail());
            throw new UserDoesNotExistException("Password doesn't match");
        }

        String accessToken = tokenService.generateAccessTokenForAdmin(customer);
        String refreshToken = tokenService.generateRefreshTokenForAdmin(customer).getToken();

        log.info("Admin logged in successfully: {}", customer.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
