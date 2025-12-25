package com.resturant.Restaurant_Application.customer.security;

import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.customer.service.CustomerCreationService;
import com.resturant.Restaurant_Application.restaurant.admin.AdminEntity;
import com.resturant.Restaurant_Application.restaurant.admin.AdminService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final TokenService tokenService;

    public SecurityConfig(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    // ===================== Security Filter Chain =====================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CustomerCreationService customerService,
                                                   AdminService adminService) throws Exception {

        http
                .cors(cors -> {}) // default CORS
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/admin/login",
                                "/api/admin/register",
                                "/api/v1/customer/register",
                                "/api/v1/customer/login",
                                "/welcome"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(customerService, adminService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(CustomerCreationService customerService,
                                                           AdminService adminService) {
        return new JwtAuthenticationFilter(tokenService, customerService, adminService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ===================== JWT AUTHENTICATION FILTER =====================
    public static class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final TokenService tokenService;
        private final CustomerCreationService customerService;
        private final AdminService adminService;

        public JwtAuthenticationFilter(TokenService tokenService,
                                       CustomerCreationService customerService,
                                       AdminService adminService) {
            this.tokenService = tokenService;
            this.customerService = customerService;
            this.adminService = adminService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            String token = parseJwt(request);

            if (token != null && tokenService.validateAccessToken(token)) {
                String email = tokenService.getEmailFromAccessToken(token);
                String role = tokenService.getRoleFromAccessToken(token); // "ADMIN" or "CUSTOMER"

                UserDetails userDetails;

                if ("ADMIN".equalsIgnoreCase(role)) {
                    AdminEntity admin = adminService.getUserByEmail(email);
                    userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(admin.getEmail())
                            .password(admin.getPassword()) // must be BCrypt encoded
                            .roles(admin.getRole().name())
                            .build();
                } else {
                    CustomerEntity customer = customerService.getUserByEmail(email);
                    userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(customer.getEmail())
                            .password(customer.getPassword()) // must be BCrypt encoded
                            .roles(customer.getRole().name())
                            .build();
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);
        }

        // ===================== Helper: Parse JWT =====================
        private String parseJwt(HttpServletRequest request) {
            String headerAuth = request.getHeader("Authorization");
            if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
                return headerAuth.substring(7);
            }
            return null;
        }
    }

    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        return username -> {
            throw new RuntimeException("No default user. Use JWT login.");
        };
    }

}
