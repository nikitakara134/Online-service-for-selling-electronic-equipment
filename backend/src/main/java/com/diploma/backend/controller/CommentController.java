package com.diploma.backend.controller;

import com.diploma.backend.model.Comment;
import com.diploma.backend.model.Review;
import com.diploma.backend.model.User;
import com.diploma.backend.repository.CommentRepo;
import com.diploma.backend.repository.ReviewRepo;
import com.diploma.backend.repository.UserRepo;
import com.diploma.backend.services.MailSenderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentRepo commentRepo;
    private final ReviewRepo reviewRepo;
    private final UserRepo userRepo;
    private final MailSenderService mailSender;

    public CommentController(CommentRepo commentRepo, ReviewRepo reviewRepo, UserRepo userRepo, MailSenderService mailSender) {
        this.commentRepo = commentRepo;
        this.reviewRepo = reviewRepo;
        this.userRepo = userRepo;
        this.mailSender = mailSender;
    }

    @PostMapping("/create/{reviewId}")
    public String addComment(
            @PathVariable Long reviewId,
            @RequestParam String text,
            @RequestParam(required = false) Long parentId,
            Principal principal
    ) {
        Review review = reviewRepo.findById(reviewId).orElseThrow();
        User author = userRepo.findByEmail(principal.getName());

        Comment comment = new Comment();
        comment.setText(text);
        comment.setUser(author);
        comment.setReview(review);

        if (parentId != null) {
            Comment parentComment = commentRepo.findById(parentId).orElse(null);
            if (parentComment != null) {
                comment.setParent(parentComment);
            }
        }

        commentRepo.save(comment);

        // Сповіщення на пошту (тільки для кореневих коментарів)
        if (parentId == null && !review.getUser().getEmail().equals(author.getEmail())) {
            sendNotification(review.getUser(), author, review.getProduct().getTitle(), text, review.getProduct().getId());
        }

        return "OK";
    }

    @PostMapping("/{commentId}/like")
    public String toggleLike(@PathVariable Long commentId, Principal principal) {
        Comment comment = commentRepo.findById(commentId).orElseThrow();
        User user = userRepo.findByEmail(principal.getName());

        if (comment.getLikedBy().contains(user)) {
            comment.getLikedBy().remove(user);
        } else {
            comment.getLikedBy().add(user);
        }

        commentRepo.save(comment);
        return "OK";
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteComment(@PathVariable Long id) {
        commentRepo.deleteById(id);
        return "Deleted";
    }

    private void sendNotification(User receiver, User author, String productTitle, String text, Long productId) {
        new Thread(() -> {
            try {
                String link = "http://localhost:3000/product/" + productId;
                String message = String.format("Привіт, %s!\n%s відповів на твій відгук.\n\nТекст: %s\n\n%s",
                        receiver.getName(), author.getName(), text, link);
                mailSender.send(receiver.getEmail(), "Нова відповідь!", message);
            } catch (Exception e) {
                System.err.println("Mail error: " + e.getMessage());
            }
        }).start();
    }
}