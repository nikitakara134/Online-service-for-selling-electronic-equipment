package com.diploma.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String phoneNumber;
    private String address;

    @Column(length = 5000)
    private String itemsDetails;

    private int totalPrice;

    @ManyToOne(fetch = FetchType.EAGER) // Змінив на EAGER, щоб простіше діставати User для адмінки
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime dateOfCreated;

    // Статус замовлення
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @PrePersist
    private void init() {
        dateOfCreated = LocalDateTime.now();
        if (status == null) status = OrderStatus.NEW; // Статус за замовчуванням
    }

    // Геттери і Сеттери
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getItemsDetails() { return itemsDetails; }
    public void setItemsDetails(String itemsDetails) { this.itemsDetails = itemsDetails; }
    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getDateOfCreated() { return dateOfCreated; }
    public void setDateOfCreated(LocalDateTime dateOfCreated) { this.dateOfCreated = dateOfCreated; }
}