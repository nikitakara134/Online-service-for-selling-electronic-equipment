package com.diploma.backend.controller;

import com.diploma.backend.model.Image;
import com.diploma.backend.model.Product;
import com.diploma.backend.model.Review;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.ProductRepo;
import com.diploma.backend.repository.ReviewRepo;
import com.diploma.backend.repository.UserRepo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final ReviewRepo reviewRepo;

    // --- РУЧНИЙ КОНСТРУКТОР (Замість @RequiredArgsConstructor) ---
    public ReviewController(ProductRepo productRepo, UserRepo userRepo, ReviewRepo reviewRepo) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.reviewRepo = reviewRepo;
    }
    // ------------------------------------------------------------

    @PostMapping("/create/{productId}")
    public String addReview(@PathVariable Long productId,
                            @RequestParam("text") String text,
                            @RequestParam("rating") int rating,
                            @RequestParam(value = "file", required = false) MultipartFile file,
                            Principal principal) throws Exception {

        Product product = productRepo.findById(productId).orElseThrow();
        User user = userRepo.findByEmail(principal.getName());

        Review review = new Review();
        review.setText(text);
        review.setRating(rating);
        review.setProduct(product);
        review.setUser(user);

        if (file != null && !file.isEmpty()) {
            Image image = new Image();
            image.setName(file.getName());
            image.setOriginalFileName(file.getOriginalFilename());
            image.setContentType(file.getContentType());
            image.setSize(file.getSize());
            image.setBytes(file.getBytes());

            // Використовуємо метод, який ми додали в Review.java
            review.addImageToReview(image);
        }

        reviewRepo.save(review);
        return "Відгук додано!";
    }
}