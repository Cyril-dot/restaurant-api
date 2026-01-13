package com.resturant.Restaurant_Application.customer.controller;

import com.resturant.Restaurant_Application.customer.entity.Enum.Role;

public record CustomerResponseDTO(Integer id, String name, String email, String phoneNumber, Role role) {}
