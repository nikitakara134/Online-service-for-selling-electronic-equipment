package com.diploma.backend.services;

import com.diploma.backend.dto.OrderRequest;
import com.diploma.backend.model.Order;
import com.diploma.backend.model.OrderStatus;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.OrderRepo;
import com.diploma.backend.repository.UserRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepo orderRepo;
    private final UserRepo userRepo;
    private final MailSenderService mailSender;
    private final CartService cartService;

    public OrderService(OrderRepo orderRepo, UserRepo userRepo, MailSenderService mailSender, CartService cartService) {
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.mailSender = mailSender;
        this.cartService = cartService;
    }

    // Створення замовлення
    public void createOrder(OrderRequest request, String userEmail) {
        User user = userRepo.findByEmail(userEmail);
        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(request.customerName);
        order.setPhoneNumber(request.phoneNumber);
        order.setAddress(request.address);
        order.setStatus(OrderStatus.NEW); // Початковий статус

        StringBuilder itemsText = new StringBuilder();
        int total = 0;

        if (request.items != null) {
            for (OrderRequest.CartItemDto item : request.items) {
                int sum = item.price * item.qty;
                total += sum;
                itemsText.append(String.format("- %s (x%d) — %d ₴\n", item.title, item.qty, sum));
            }
        }

        order.setItemsDetails(itemsText.toString());
        order.setTotalPrice(total);

        orderRepo.save(order);

        // Очищаємо кошик після успішного замовлення
        cartService.clearCart(userEmail);

        sendReceipt(user.getEmail(), order);
    }

    // Історія користувача
    public List<Order> getUserOrders(String email) {
        return orderRepo.findByUserEmail(email);
    }

    // Адмінка: всі замовлення
    public List<Order> getAllOrders() {
        return orderRepo.findAllByOrderByDateOfCreatedDesc();
    }

    // Адмінка: Зміна статусу
    public void updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepo.findById(orderId).orElseThrow();
        order.setStatus(newStatus);
        orderRepo.save(order);

        // Відправка листа про зміну статусу
        String subject = "Статус замовлення #" + order.getId() + " змінено!";
        String message = String.format("Ваше замовлення отримало новий статус: %s", newStatus);
        mailSender.send(order.getUser().getEmail(), subject, message);
    }

    private void sendReceipt(String email, Order order) {
        String subject = "Ваше замовлення #" + order.getId() + " прийнято!";
        String message = "Дякуємо! Сума: " + order.getTotalPrice() + " грн.\n" + order.getItemsDetails();
        mailSender.send(email, subject, message);
    }
}