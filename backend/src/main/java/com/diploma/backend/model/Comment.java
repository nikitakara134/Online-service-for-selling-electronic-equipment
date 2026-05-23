package com.diploma.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String text;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    @JsonIgnore
    private Review review;

    private LocalDateTime dateOfCreated;

    // --- БАТЬКІВСЬКИЙ КОМЕНТАР ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore // Ігноруємо сам об'єкт, щоб не було рекурсії
    private Comment parent;

    // --- ВАЖЛИВО: Передаємо ID батька в JSON ---
    @JsonProperty("parentId")
    public Long getParentId() {
        return parent != null ? parent.getId() : null;
    }

    // --- Дочірні коментарі ---
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("dateOfCreated ASC")
    private List<Comment> children = new ArrayList<>();

    // --- Лайки ---
    @ManyToMany
    @JoinTable(
            name = "comment_likes",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedBy = new HashSet<>();

    @PrePersist
    private void init() {
        dateOfCreated = LocalDateTime.now();
    }

    // --- Геттери та Сеттери ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }
    public LocalDateTime getDateOfCreated() { return dateOfCreated; }
    public void setDateOfCreated(LocalDateTime dateOfCreated) { this.dateOfCreated = dateOfCreated; }
    public Comment getParent() { return parent; }
    public void setParent(Comment parent) { this.parent = parent; }
    public List<Comment> getChildren() { return children; }
    public void setChildren(List<Comment> children) { this.children = children; }
    public Set<User> getLikedBy() { return likedBy; }
    public void setLikedBy(Set<User> likedBy) { this.likedBy = likedBy; }
}