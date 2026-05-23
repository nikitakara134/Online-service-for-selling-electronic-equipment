package com.diploma.backend.controller;

import com.diploma.backend.model.Image;
import com.diploma.backend.repository.ImageRepo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

@RestController
public class ImageController {
    private final ImageRepo imageRepository;

    // --- Додаємо конструктор вручну ---
    public ImageController(ImageRepo imageRepository) {
        this.imageRepository = imageRepository;
    }

    @GetMapping("/api/images/{id}")
    public ResponseEntity<?> getImageById(@PathVariable Long id) {
        Image image = imageRepository.findById(id).orElse(null);
        if (image == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header("fileName", image.getOriginalFileName())
                .contentType(MediaType.valueOf(image.getContentType()))
                .contentLength(image.getSize())
                .body(new InputStreamResource(new ByteArrayInputStream(image.getBytes())));
    }
}