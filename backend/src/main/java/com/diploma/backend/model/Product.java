package com.diploma.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 5000)
    private String description;

    private int price;
    private String city;
    private String category;

    // --- ВИПРАВЛЕННЯ: Додано зв'язок з користувачем ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    // --------------------------------------------------

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            mappedBy = "product", orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    // Додамо також список відгуків, щоб уникнути майбутніх помилок
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "product")
    private List<Review> reviews = new ArrayList<>();

    private Long previewImageId;
    private LocalDateTime dateOfCreated;

    @PrePersist
    private void init() {
        dateOfCreated = LocalDateTime.now();
    }

    public void addImageToProduct(Image image) {
        image.setProduct(this);
        images.add(image);
    }

    // --- ГЕТТЕРИ І СЕТТЕРИ ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // --- ГЕТТЕР І СЕТТЕР ДЛЯ USER (Виправляють помилку) ---
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    // -----------------------------------------------------

    public List<Image> getImages() { return images; }
    public void setImages(List<Image> images) { this.images = images; }

    public Long getPreviewImageId() { return previewImageId; }
    public void setPreviewImageId(Long previewImageId) { this.previewImageId = previewImageId; }

    public LocalDateTime getDateOfCreated() { return dateOfCreated; }
    public void setDateOfCreated(LocalDateTime dateOfCreated) { this.dateOfCreated = dateOfCreated; }

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}