package com.diploma.backend.controller;

import com.diploma.backend.model.Image;
import com.diploma.backend.model.Product;
import com.diploma.backend.model.Review;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.ProductRepo;
import com.diploma.backend.repository.UserRepo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepo productRepo;
    private final UserRepo userRepo;

    // Ми залишили тільки ті репозиторії, які реально використовуються в коді
    public ProductController(ProductRepo productRepo, UserRepo userRepo) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    // --- РЕКОМЕНДАЦІЇ ---
    @GetMapping("/recommendations")
    public List<Product> getRecommendations(Principal principal) {
        // 1. Якщо гість - повертаємо випадкові
        if (principal == null) {
            return getDefaultRecommendations();
        }

        User user = userRepo.findByEmail(principal.getName());
        List<Review> userReviews = user.getReviews();

        // 2. Якщо немає відгуків - повертаємо випадкові
        if (userReviews == null || userReviews.isEmpty()) {
            return getDefaultRecommendations();
        }

        // 3. Аналіз категорій
        Map<String, Integer> categoryCount = new HashMap<>();
        List<Long> reviewedProductIds = new ArrayList<>();

        for (Review review : userReviews) {
            if (review.getProduct() != null) {
                String cat = review.getProduct().getCategory();
                categoryCount.put(cat, categoryCount.getOrDefault(cat, 0) + 1);
                reviewedProductIds.add(review.getProduct().getId());
            }
        }

        // Якщо після аналізу не знайшли категорій (наприклад, товари були видалені)
        if (categoryCount.isEmpty()) {
            return getDefaultRecommendations();
        }

        // Знаходимо улюблену категорію
        String favoriteCategory = Collections.max(categoryCount.entrySet(), Map.Entry.comparingByValue()).getKey();

        // 4. Пошук (тепер ми точно знаємо, що reviewedProductIds не пустий, якщо дійшли сюди)
        List<Product> recommendations = productRepo.findByCategoryAndIdNotIn(favoriteCategory, reviewedProductIds);

        // 5. Доповнення списку, якщо мало рекомендацій
        if (recommendations.size() < 4) {
            List<Product> all = productRepo.findAll();
            for (Product p : all) {
                if (recommendations.size() >= 4) break;
                if (!recommendations.contains(p) && !reviewedProductIds.contains(p.getId())) {
                    recommendations.add(p);
                }
            }
        }

        return recommendations.stream().limit(4).collect(Collectors.toList());
    }

    private List<Product> getDefaultRecommendations() {
        return productRepo.findAll().stream().limit(4).collect(Collectors.toList());
    }

    // --- СТАНДАРТНІ МЕТОДИ ---

    @GetMapping
    public List<Product> getProducts() {
        return productRepo.findAll();
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productRepo.findById(id).orElseThrow();
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createProduct(
            @RequestParam("file") MultipartFile[] files,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam int price,
            @RequestParam String city,
            @RequestParam String category,
            Principal principal
    ) throws IOException {
        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setCity(city);
        product.setCategory(category);

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                if (!file.isEmpty()) {
                    Image image = toImageEntity(file);
                    if (i == 0) image.setPreviewImage(true);
                    product.addImageToProduct(image);
                }
            }
        }
        Product savedProduct = productRepo.save(product);
        if (!savedProduct.getImages().isEmpty()) {
            savedProduct.setPreviewImageId(savedProduct.getImages().get(0).getId());
            productRepo.save(savedProduct);
        }
        return "OK";
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProduct(@PathVariable Long id) {
        productRepo.deleteById(id);
        return "Deleted";
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateProduct(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile[] files,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam int price,
            @RequestParam String city,
            @RequestParam String category
    ) throws IOException {
        Product product = productRepo.findById(id).orElseThrow();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setCity(city);
        product.setCategory(category);

        if (files != null && files.length > 0 && !files[0].isEmpty()) {
            product.getImages().clear();
            for (int i = 0; i < files.length; i++) {
                Image image = toImageEntity(files[i]);
                if (i == 0) image.setPreviewImage(true);
                product.addImageToProduct(image);
            }
        }
        Product saved = productRepo.save(product);
        if (!saved.getImages().isEmpty()) {
            saved.setPreviewImageId(saved.getImages().get(0).getId());
            productRepo.save(saved);
        }
        return "Updated";
    }

    private Image toImageEntity(MultipartFile file) throws IOException {
        Image image = new Image();
        image.setName(file.getName());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setBytes(file.getBytes());
        return image;
    }
}