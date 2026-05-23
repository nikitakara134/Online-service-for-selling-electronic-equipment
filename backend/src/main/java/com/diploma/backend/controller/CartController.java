package com.diploma.backend.controller;

import com.diploma.backend.dto.OrderRequest;
import com.diploma.backend.model.Cart;
import com.diploma.backend.services.CartService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Cart getCart(Principal principal) {
        return cartService.getCartByUser(principal.getName());
    }

    @PostMapping("/add")
    public String addToCart(@RequestBody OrderRequest.CartItemDto item, Principal principal) {
        cartService.addToCart(principal.getName(), item);
        return "Added";
    }

    @DeleteMapping("/remove/{itemId}")
    public String removeFromCart(@PathVariable Long itemId, Principal principal) {
        cartService.removeFromCart(principal.getName(), itemId);
        return "Removed";
    }
}