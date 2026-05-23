package com.diploma.backend.repository;

import com.diploma.backend.model.Cart;
import com.diploma.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepo extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}