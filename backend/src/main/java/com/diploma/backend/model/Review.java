package com.diploma.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private int rating;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnore // ВАЖЛИВО: Залишаємо, щоб не ламався JSON
    private Product product;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "review")
    private List<Image> images = new ArrayList<>();

    // --- НОВЕ ПОЛЕ: Список коментарів до цього відгуку ---
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "review")
    private List<Comment> comments = new ArrayList<>();

    private LocalDateTime dateOfCreated;

    @PrePersist
    private void init() {
        dateOfCreated = LocalDateTime.now();
    }

    public void addImageToReview(Image image) {
        image.setReview(this);
        images.add(image);
    }

    // --- ГЕТТЕРИ ТА СЕТТЕРИ ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public List<Image> getImages() { return images; }
    public void setImages(List<Image> images) { this.images = images; }

    // Геттер і сетер для коментарів
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public LocalDateTime getDateOfCreated() { return dateOfCreated; }
    public void setDateOfCreated(LocalDateTime dateOfCreated) { this.dateOfCreated = dateOfCreated; }
}