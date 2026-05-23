package com.diploma.backend;

import com.diploma.backend.services.CustomUserDetailsService;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepo userRepo;
    @InjectMocks private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_UserFound_ReturnsUserDetails() {
        User user = new User(); user.setEmail("test@admin.com");
        when(userRepo.findByEmail("test@admin.com")).thenReturn(user);

        UserDetails result = service.loadUserByUsername("test@admin.com");
        assertNotNull(result);
    }

    @Test
    void loadUserByUsername_UserNotFound_ReturnsNull() {
        when(userRepo.findByEmail("unknown@admin.com")).thenReturn(null);
        UserDetails result = service.loadUserByUsername("unknown@admin.com");
        assertNull(result);
    }

    @Test
    void loadUserByUsername_NullEmail_ReturnsNull() {
        when(userRepo.findByEmail(null)).thenReturn(null);
        UserDetails result = service.loadUserByUsername(null);
        assertNull(result);
    }
}