package com.diploma.backend.services;

import com.diploma.backend.model.Image;
import com.diploma.backend.model.Product;
import com.diploma.backend.repository.ProductRepo;
import org.slf4j.Logger;         // <--- Додано імпорт
import org.slf4j.LoggerFactory;  // <--- Додано імпорт
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {
    // 1. Створюємо логер вручну
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepo productRepository;

    // 2. Створюємо конструктор вручну (щоб ініціалізувати productRepository)
    public ProductService(ProductRepo productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> listProducts(String title) {
        if (title != null && !title.isEmpty()) return productRepository.findByTitleContainingIgnoreCase(title);
        return productRepository.findAll();
    }

    public void saveProduct(Product product, MultipartFile file1, MultipartFile file2, MultipartFile file3) throws IOException {
        Image image1;
        Image image2;
        Image image3;
        if (file1 != null && file1.getSize() != 0) {
            image1 = toImageEntity(file1);
            image1.setPreviewImage(true);
            product.addImageToProduct(image1);
        }
        if (file2 != null && file2.getSize() != 0) {
            image2 = toImageEntity(file2);
            product.addImageToProduct(image2);
        }
        if (file3 != null && file3.getSize() != 0) {
            image3 = toImageEntity(file3);
            product.addImageToProduct(image3);
        }

        log.info("Saving new Product. Title: {}", product.getTitle());
        Product productFromDb = productRepository.save(product);
        if (!productFromDb.getImages().isEmpty()) {
            productFromDb.setPreviewImageId(productFromDb.getImages().get(0).getId());
        }
        productRepository.save(product);
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

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}