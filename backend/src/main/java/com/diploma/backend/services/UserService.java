package com.diploma.backend.services;

import com.diploma.backend.model.Role;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    // 1. Створюємо логер вручну (замість @Slf4j)
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    // 2. Створюємо конструктор вручну (замість @RequiredArgsConstructor)
    public UserService(UserRepo userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) return false;

        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getRoles().add(Role.ROLE_USER);

        log.info("Saving new User with email: {}", user.getEmail());
        userRepository.save(user);
        return true;
    }
}