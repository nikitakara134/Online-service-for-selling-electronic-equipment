package com.diploma.backend.dto;

import java.util.List;

public class OrderRequest {
    public String customerName;
    public String phoneNumber;
    public String address;
    public List<CartItemDto> items;

    // Вкладений клас для опису одного товару в кошику
    public static class CartItemDto {
        public String title;
        public int price;
        public int qty;
    }
}