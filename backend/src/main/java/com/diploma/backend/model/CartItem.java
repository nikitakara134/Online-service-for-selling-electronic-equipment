package com.diploma.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int price;
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    private Cart cart;

    // Конструктори
    public CartItem() {}
    public CartItem(String title, int price, int quantity, Cart cart) {
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.cart = cart;
    }

    // Геттери/Сеттери
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
}