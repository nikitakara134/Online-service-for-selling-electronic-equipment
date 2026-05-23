package com.diploma.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // <--- ВАЖЛИВИЙ ІМПОРТ
import jakarta.persistence.*;

@Entity
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String originalFileName;
    private Long size;
    private String contentType;
    private boolean isPreviewImage;

    @Lob
    @Column(columnDefinition = "longblob")
    private byte[] bytes;

    // --- РОЗРИВАЄМО КОЛО З ТОВАРОМ ---
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JsonIgnore
    private Product product;

    // --- РОЗРИВАЄМО КОЛО З ВІДГУКОМ ---
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JsonIgnore
    private Review review;

    // --- ГЕТТЕРИ ТА СЕТТЕРИ ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public boolean isPreviewImage() { return isPreviewImage; }
    public void setPreviewImage(boolean previewImage) { isPreviewImage = previewImage; }

    public byte[] getBytes() { return bytes; }
    public void setBytes(byte[] bytes) { this.bytes = bytes; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }
}