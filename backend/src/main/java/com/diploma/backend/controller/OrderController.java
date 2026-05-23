package com.diploma.backend.controller;

import com.diploma.backend.dto.OrderRequest;
import com.diploma.backend.model.Order;
import com.diploma.backend.model.OrderStatus;
import com.diploma.backend.services.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Оформлення замовлення
    @PostMapping("/create")
    public String createOrder(@RequestBody OrderRequest request, Principal principal) {
        orderService.createOrder(request, principal.getName());
        return "OK";
    }

    // Історія замовлень (для профілю)
    @GetMapping("/my")
    public List<Order> getMyOrders(Principal principal) {
        return orderService.getUserOrders(principal.getName());
    }

    // --- АДМІН ЧАСТИНА ---

    // Отримати всі замовлення
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // Змінити статус
    @PostMapping("/admin/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
        return "Status updated";
    }
}