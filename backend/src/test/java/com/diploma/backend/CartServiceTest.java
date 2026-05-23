package com.diploma.backend;

import com.diploma.backend.services.CartService;
import com.diploma.backend.dto.OrderRequest;
import com.diploma.backend.model.Cart;
import com.diploma.backend.model.CartItem;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.CartRepo;
import com.diploma.backend.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepo cartRepo;
    @Mock private UserRepo userRepo;
    @InjectMocks private CartService cartService;

    @Test
    void getCartByUser_CartExists_ReturnsCart() {
        User user = new User(); user.setEmail("user@test.com");
        Cart cart = new Cart(); cart.setUser(user);
        when(userRepo.findByEmail("user@test.com")).thenReturn(user);
        when(cartRepo.findByUser(user)).thenReturn(Optional.of(cart));

        Cart result = cartService.getCartByUser("user@test.com");
        assertNotNull(result);
    }

    @Test
    void getCartByUser_CartNotExists_CreatesNewCart() {
        User user = new User(); user.setEmail("new@test.com");
        when(userRepo.findByEmail("new@test.com")).thenReturn(user);
        when(cartRepo.findByUser(user)).thenReturn(Optional.empty());
        when(cartRepo.save(any(Cart.class))).thenAnswer(i -> i.getArguments()[0]);

        Cart result = cartService.getCartByUser("new@test.com");
        assertNotNull(result);
        verify(cartRepo, times(1)).save(any(Cart.class));
    }

    @Test
    void getCartByUser_UserNotFound_ThrowsException() {
        when(userRepo.findByEmail("unknown@test.com")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> cartService.getCartByUser("unknown@test.com"));
    }

    @Test
    void addToCart_NewItem_AddsToList() {
        User user = new User(); user.setEmail("user@test.com");
        Cart cart = new Cart(); cart.setUser(user); cart.setItems(new ArrayList<>());
        when(userRepo.findByEmail("user@test.com")).thenReturn(user);
        when(cartRepo.findByUser(user)).thenReturn(Optional.of(cart));

        OrderRequest.CartItemDto dto = new OrderRequest.CartItemDto();
        dto.title = "Laptop"; dto.price = 1000; dto.qty = 1;

        cartService.addToCart("user@test.com", dto);
        assertEquals(1, cart.getItems().size());
        verify(cartRepo, times(1)).save(cart);
    }

    @Test
    void addToCart_ExistingItem_IncreasesQuantity() {
        User user = new User(); user.setEmail("user@test.com");
        Cart cart = new Cart(); cart.setUser(user);
        CartItem existingItem = new CartItem("Mouse", 50, 2, cart);
        cart.setItems(new ArrayList<>(java.util.List.of(existingItem)));

        when(userRepo.findByEmail("user@test.com")).thenReturn(user);
        when(cartRepo.findByUser(user)).thenReturn(Optional.of(cart));

        OrderRequest.CartItemDto dto = new OrderRequest.CartItemDto();
        dto.title = "Mouse"; dto.price = 50; dto.qty = 3;

        cartService.addToCart("user@test.com", dto);
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    void removeFromCart_ItemExists_RemovesIt() {
        User user = new User(); user.setEmail("user@test.com");
        Cart cart = new Cart(); cart.setUser(user);
        CartItem item1 = new CartItem("Phone", 500, 1, cart); item1.setId(10L);
        cart.setItems(new ArrayList<>(java.util.List.of(item1)));

        when(userRepo.findByEmail("user@test.com")).thenReturn(user);
        when(cartRepo.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.removeFromCart("user@test.com", 10L);
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void removeFromCart_ItemNotExists_DoesNothing() {
        User user = new User(); user.setEmail("user@test.com");
        Cart cart = new Cart(); cart.setUser(user);
        CartItem item1 = new CartItem("Phone", 500, 1, cart); item1.setId(10L);
        cart.setItems(new ArrayList<>(java.util.List.of(item1)));

        when(userRepo.findByEmail("user@test.com")).thenReturn(user);
        when(cartRepo.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.removeFromCart("user@test.com", 99L);
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void clearCart_Success_EmptiesList() {
        User user = new User(); user.setEmail("user@test.com");
        Cart cart = new Cart(); cart.setUser(user);
        cart.setItems(new ArrayList<>(java.util.List.of(new CartItem(), new CartItem())));

        when(userRepo.findByEmail("user@test.com")).thenReturn(user);
        when(cartRepo.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.clearCart("user@test.com");
        assertTrue(cart.getItems().isEmpty());
    }
}