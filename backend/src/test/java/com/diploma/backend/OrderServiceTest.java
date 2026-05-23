package com.diploma.backend;

import com.diploma.backend.services.CartService;
import com.diploma.backend.services.MailSenderService;
import com.diploma.backend.services.OrderService;
import com.diploma.backend.dto.OrderRequest;
import com.diploma.backend.model.Order;
import com.diploma.backend.model.OrderStatus;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.OrderRepo;
import com.diploma.backend.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepo orderRepo;
    @Mock private UserRepo userRepo;
    @Mock private MailSenderService mailSender;
    @Mock private CartService cartService;

    @InjectMocks private OrderService orderService;

    @Test
    void createOrder_Success_CalculatesTotalAndSendsEmail() {
        User user = new User(); user.setEmail("client@test.com");
        OrderRequest request = new OrderRequest();
        request.customerName = "Ivan Ivanov";

        OrderRequest.CartItemDto item1 = new OrderRequest.CartItemDto();
        item1.title = "Laptop"; item1.price = 20000; item1.qty = 1;
        request.items = Arrays.asList(item1);

        when(userRepo.findByEmail("client@test.com")).thenReturn(user);

        orderService.createOrder(request, "client@test.com");

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepo, times(1)).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.NEW, savedOrder.getStatus());
        assertEquals(20000, savedOrder.getTotalPrice());

        verify(cartService, times(1)).clearCart("client@test.com");
        verify(mailSender, times(1)).send(eq("client@test.com"), anyString(), anyString());
    }

    @Test
    void createOrder_NullItems_SavesOrderWithZeroTotal() {
        User user = new User(); user.setEmail("client@test.com");
        OrderRequest request = new OrderRequest(); request.items = null;

        when(userRepo.findByEmail("client@test.com")).thenReturn(user);

        orderService.createOrder(request, "client@test.com");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepo).save(captor.capture());
        assertEquals(0, captor.getValue().getTotalPrice());
    }

    @Test
    void getUserOrders_Success_ReturnsList() {
        when(orderRepo.findByUserEmail("user@test.com")).thenReturn(Arrays.asList(new Order()));
        assertEquals(1, orderService.getUserOrders("user@test.com").size());
    }

    @Test
    void getUserOrders_NoOrders_ReturnsEmptyList() {
        when(orderRepo.findByUserEmail("new@test.com")).thenReturn(Collections.emptyList());
        assertTrue(orderService.getUserOrders("new@test.com").isEmpty());
    }

    @Test
    void getAllOrders_Success_ReturnsList() {
        when(orderRepo.findAllByOrderByDateOfCreatedDesc()).thenReturn(Arrays.asList(new Order(), new Order()));
        assertEquals(2, orderService.getAllOrders().size());
    }

    @Test
    void getAllOrders_EmptyDatabase_ReturnsEmptyList() {
        when(orderRepo.findAllByOrderByDateOfCreatedDesc()).thenReturn(Collections.emptyList());
        assertTrue(orderService.getAllOrders().isEmpty());
    }

    @Test
    void updateStatus_Success_UpdatesAndSendsEmail() {
        User user = new User(); user.setEmail("user@test.com");
        Order order = new Order(); order.setId(1L); order.setUser(user); order.setStatus(OrderStatus.NEW);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        orderService.updateStatus(1L, OrderStatus.PROCESSING);

        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        verify(orderRepo, times(1)).save(order);
        verify(mailSender, times(1)).send(eq("user@test.com"), anyString(), anyString());
    }

    @Test
    void updateStatus_OrderNotFound_ThrowsException() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> orderService.updateStatus(99L, OrderStatus.SHIPPED));
    }
}