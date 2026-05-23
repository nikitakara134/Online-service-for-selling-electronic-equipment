package com.diploma.backend.controller;

import com.diploma.backend.model.User;
import com.diploma.backend.repository.ProductRepo;
import com.diploma.backend.repository.UserRepo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Тільки адмін сюди зайде
public class AdminController {
    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    public AdminController(UserRepo userRepo, ProductRepo productRepo) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("usersCount", userRepo.count());
        stats.put("productsCount", productRepo.count());
        return stats;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}