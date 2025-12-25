package com.resturant.Restaurant_Application;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // Public endpoint - no authentication required
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to Restaurant";
    }

    // ADMIN only
    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTest() {
        return "Hello Admin! You have access to admin endpoints.";
    }

    // USER only
    @GetMapping("/user/test")
    @PreAuthorize("hasRole('USER')")
    public String userTest() {
        return "Hello User! You have access to user endpoints.";
    }

    // Both ADMIN and USER
    @GetMapping("/common/test")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public String commonTest() {
        return "Hello! Both Admin and User can access this.";
    }
}
