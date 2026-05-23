package com.diploma.backend.repository;

import com.diploma.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Тут порожньо, бо Spring Data JPA вже має всі методи всередині
}