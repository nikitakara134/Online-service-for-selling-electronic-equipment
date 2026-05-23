package com.diploma.backend.model;

public enum OrderStatus {
    NEW,        // Нове
    PROCESSING, // В обробці
    SHIPPED,    // Відправлено
    DELIVERED,  // Доставлено
    CANCELED    // Скасовано
}