package com.diploma.backend.services;

import com.diploma.backend.dto.OrderRequest;
import com.diploma.backend.model.Cart;
import com.diploma.backend.model.CartItem;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.CartRepo;
import com.diploma.backend.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {
    private final CartRepo cartRepo;
    private final UserRepo userRepo;

    public CartService(CartRepo cartRepo, UserRepo userRepo) {
        this.cartRepo = cartRepo;
        this.userRepo = userRepo;
    }

    // Отримати кошик поточного користувача. Якщо немає - створити.
    @Transactional
    public Cart getCartByUser(String email) {
        User user = userRepo.findByEmail(email);
        if (user == null) throw new RuntimeException("User not found");

        return cartRepo.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepo.save(newCart);
        });
    }

    // Додати товар у кошик (Зберігаємо в БД!)
    @Transactional
    public void addToCart(String email, OrderRequest.CartItemDto itemDto) {
        Cart cart = getCartByUser(email);

        // Перевіряємо, чи є вже такий товар, щоб збільшити кількість
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getTitle().equals(itemDto.title))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + itemDto.qty);
        } else {
            CartItem newItem = new CartItem(itemDto.title, itemDto.price, itemDto.qty, cart);
            cart.getItems().add(newItem);
        }
        cartRepo.save(cart);
    }

    // Очистити кошик (після замовлення)
    @Transactional
    public void clearCart(String email) {
        Cart cart = getCartByUser(email);
        cart.getItems().clear();
        cartRepo.save(cart);
    }

    // Видалення товару з кошика
    @Transactional
    public void removeFromCart(String email, Long itemId) {
        Cart cart = getCartByUser(email);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        cartRepo.save(cart);
    }
}