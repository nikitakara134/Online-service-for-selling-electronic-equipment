package com.diploma.backend.configuration;

import com.diploma.backend.model.Image;
import com.diploma.backend.model.Product;
import com.diploma.backend.model.Role;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.ProductRepo;
import com.diploma.backend.repository.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    // Ліміт товарів на одну категорію
    private static final int PRODUCTS_PER_CATEGORY_LIMIT = 20;

    public DataInitializer(UserRepo userRepo, ProductRepo productRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepo.count() == 0) {
            System.out.println("ЗАВАНТАЖЕННЯ ДАНИХ (ЛІМІТ 20/КАТЕГОРІЯ)...");

            User admin = createUser("admin@shop.com", "admin", "Admin Boss", Role.ROLE_ADMIN);
            User user = createUser("user@shop.com", "user", "Simple User", Role.ROLE_USER);

            // Лічильник для категорій
            Map<String, Integer> categoryCounts = new HashMap<>();
            categoryCounts.put("Ноутбуки", 0);
            categoryCounts.put("Телефони", 0);
            categoryCounts.put("Побутова техніка", 0);
            categoryCounts.put("Аксесуари", 0);

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new ClassPathResource("ElectronicsData.csv").getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                boolean isFirstLine = true;

                while ((line = br.readLine()) != null) {
                    if (isFirstLine) { isFirstLine = false; continue; }

                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (data.length < 7) continue;

                    String rawCategory = stripQuotes(data[0]);
                    String rawPrice = stripQuotes(data[1]);
                    String title = stripQuotes(data[4]);
                    String description = stripQuotes(data[6]);

                    if (title.isEmpty() || rawPrice.isEmpty()) continue;

                    CategoryMapping mapping = mapCategory(rawCategory);
                    String currentCategory = mapping.uaCategory;

                    // ПЕРЕВІРКА ЛІМІТУ: Якщо вже є 20 товарів у цій категорії - пропускаємо
                    if (categoryCounts.getOrDefault(currentCategory, 0) >= PRODUCTS_PER_CATEGORY_LIMIT) {
                        continue;
                    }

                    // Якщо всі категорії заповнені - виходимо (економія часу)
                    if (categoryCounts.values().stream().allMatch(count -> count >= PRODUCTS_PER_CATEGORY_LIMIT)) {
                        break;
                    }

                    int priceInUah = parsePrice(rawPrice);
                    User owner = random.nextBoolean() ? admin : user;

                    createProduct(owner, title, description, priceInUah, "Київ", currentCategory, mapping.imagePath);

                    // Збільшуємо лічильник
                    categoryCounts.put(currentCategory, categoryCounts.get(currentCategory) + 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("БАЗА ЗАПОВНЕНА! Додано по " + PRODUCTS_PER_CATEGORY_LIMIT + " товарів.");
        }
    }

    // --- Допоміжні методи (без змін) ---
    private String stripQuotes(String field) {
        if (field.startsWith("\"") && field.endsWith("\"")) return field.substring(1, field.length() - 1);
        return field;
    }

    private int parsePrice(String priceStr) {
        try {
            String clean = priceStr.replace("$", "").replace(",", "").trim();
            return (int) (Double.parseDouble(clean) * 40);
        } catch (Exception e) { return 0; }
    }

    static class CategoryMapping {
        String uaCategory;
        String imagePath;
        public CategoryMapping(String uaCategory, String imagePath) {
            this.uaCategory = uaCategory;
            this.imagePath = imagePath;
        }
    }

    private CategoryMapping mapCategory(String csvCategory) {
        String cat = csvCategory.toLowerCase();
        if (cat.contains("laptop") || cat.contains("notebook")) return new CategoryMapping("Ноутбуки", "images/laptop.jpg");
        else if (cat.contains("phone") || cat.contains("tablet") || cat.contains("ipad") || cat.contains("smart watch")) return new CategoryMapping("Телефони", "images/phone.jpg");
        else if (cat.contains("tv") || cat.contains("monitor") || cat.contains("projector") || cat.contains("desktop") || cat.contains("home") || cat.contains("camera")) return new CategoryMapping("Побутова техніка", "images/home.jpg"); // Об'єднали для спрощення
        else return new CategoryMapping("Аксесуари", "images/access.jpg");
    }

    private User createUser(String email, String password, String name, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setActive(true);
        user.getRoles().add(role);
        if (role == Role.ROLE_ADMIN) user.getRoles().add(Role.ROLE_USER);
        return userRepo.save(user);
    }

    private void createProduct(User user, String title, String description, int price, String city, String category, String imagePath) {
        try {
            Product product = new Product();
            product.setUser(user);
            product.setTitle(title.length() > 255 ? title.substring(0, 250) + "..." : title);
            product.setDescription(description.length() > 5000 ? description.substring(0, 4999) : description);
            product.setPrice(price);
            product.setCity(city);
            product.setCategory(category);
            product.setDateOfCreated(LocalDateTime.now().minusDays(random.nextInt(30)));

            ClassPathResource imgFile = new ClassPathResource(imagePath);
            if (imgFile.exists()) {
                byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
                Image image = new Image();
                image.setName(imgFile.getFilename());
                image.setOriginalFileName(imgFile.getFilename());
                image.setContentType("image/jpeg");
                image.setSize((long) bytes.length);
                image.setBytes(bytes);
                image.setPreviewImage(true);
                product.addImageToProduct(image);
            }
            Product savedProduct = productRepo.save(product);
            if (!savedProduct.getImages().isEmpty()) {
                savedProduct.setPreviewImageId(savedProduct.getImages().get(0).getId());
                productRepo.save(savedProduct);
            }
        } catch (Exception e) {}
    }
}