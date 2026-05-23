package com.diploma.backend.controller;

import com.diploma.backend.model.Role;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.UserRepo;
import com.diploma.backend.services.MailSenderService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;

    public UserController(UserRepo userRepo, PasswordEncoder passwordEncoder, MailSenderService mailSenderService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailSenderService = mailSenderService;
    }

    // РЕЄСТРАЦІЯ
    @PostMapping("/registration")
    public String createUser(@RequestBody User user) {
        if (userRepo.findByEmail(user.getEmail()) != null) {
            return "Error: Email already in use";
        }

        user.setActive(true); // Акаунт активний, але пошта ще ні
        user.setEmailVerified(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getRoles().add(Role.ROLE_USER);

        // Генеруємо короткий код (6 символів)
        String code = UUID.randomUUID().toString().substring(0, 6);
        user.setActivationCode(code);

        userRepo.save(user);

        // Відправляємо лист
        try {
            String message = String.format(
                    "Привіт, %s! \nТвій код для підтвердження реєстрації: %s",
                    user.getName(),
                    code
            );
            mailSenderService.send(user.getEmail(), "Activation Code", message);
        } catch (Exception e) {
            System.err.println("Помилка відправки пошти: " + e.getMessage());
            return "User created, but failed to send email";
        }

        return "OK";
    }

    // ПІДТВЕРДЖЕННЯ ПОШТИ
    @PostMapping("/verify")
    public String verifyEmail(@RequestParam String email, @RequestParam String code) {
        User user = userRepo.findByEmail(email);
        if (user != null && user.getActivationCode() != null && user.getActivationCode().equals(code)) {
            user.setEmailVerified(true);
            user.setActivationCode(null); // Видаляємо код, він більше не потрібен
            userRepo.save(user);
            return "Verified";
        }
        return "Invalid code";
    }

    // --- НОВЕ: Отримати профіль ---
    @GetMapping("/profile")
    public User getProfile(Principal principal) {
        return userRepo.findByEmail(principal.getName());
    }

    // --- НОВЕ: Редагувати профіль ---
    @PutMapping("/profile/update")
    public User updateProfile(@RequestBody User updatedData, Principal principal) {
        User user = userRepo.findByEmail(principal.getName());

        // Оновлюємо дозволені поля
        user.setName(updatedData.getName());
        user.setPhoneNumber(updatedData.getPhoneNumber());
        user.setAddress(updatedData.getAddress()); // Нове поле

        return userRepo.save(user);
    }
}