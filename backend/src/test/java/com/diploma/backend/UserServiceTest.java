package com.diploma.backend;

import com.diploma.backend.services.UserService;
import com.diploma.backend.model.Role;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success_ReturnsTrue() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("password");
        user.setRoles(new HashSet<>());

        when(userRepo.findByEmail(user.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encoded");

        assertTrue(userService.createUser(user));
        verify(userRepo, times(1)).save(user);
    }

    @Test
    void createUser_UserAlreadyExists_ReturnsFalse() {
        User existingUser = new User();
        existingUser.setEmail("test@gmail.com");
        User newUser = new User();
        newUser.setEmail("test@gmail.com");

        when(userRepo.findByEmail(newUser.getEmail())).thenReturn(existingUser);

        assertFalse(userService.createUser(newUser));
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void createUser_EncodesPasswordCorrectly() {
        User user = new User();
        user.setEmail("new@gmail.com");
        user.setPassword("rawPassword");
        user.setRoles(new HashSet<>());

        when(userRepo.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword123");

        userService.createUser(user);

        assertEquals("hashedPassword123", user.getPassword());
    }

    @Test
    void createUser_AssignsDefaultRoleUser() {
        User user = new User();
        user.setEmail("role@gmail.com");
        user.setPassword("pass");
        user.setRoles(new HashSet<>());

        when(userRepo.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        userService.createUser(user);

        assertTrue(user.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    void createUser_SetsUserActive() {
        User user = new User();
        user.setEmail("active@gmail.com");
        user.setPassword("pass");
        user.setRoles(new HashSet<>());
        user.setActive(false);

        when(userRepo.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        userService.createUser(user);

        assertTrue(user.isActive());
    }
}