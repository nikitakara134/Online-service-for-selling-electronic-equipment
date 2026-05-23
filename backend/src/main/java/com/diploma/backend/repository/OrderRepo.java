package com.diploma.backend.repository;

import com.diploma.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {
    // Знайти всі замовлення конкретного користувача (для історії)
    List<Order> findByUserEmail(String email);

    // Для адміна: всі замовлення, нові зверху
    List<Order> findAllByOrderByDateOfCreatedDesc();
}