package com.diploma.backend.repository;

import com.diploma.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Long> {

    List<Product> findByTitleContainingIgnoreCase(String title);

    List<Product> findByCategory(String category);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.id NOT IN :excludedIds")
    List<Product> findByCategoryAndIdNotIn(@Param("category") String category, @Param("excludedIds") List<Long> excludedIds);
}