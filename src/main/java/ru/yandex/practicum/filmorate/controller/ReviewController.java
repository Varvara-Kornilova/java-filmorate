package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import java.util.Collection;

/**
 * Контроллер для управления отзывами о фильмах.
 * Обрабатывает запросы к эндпоинтам /reviews.
 */
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> create(@Valid @RequestBody Review review) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(review));
    }

    @PutMapping
    public ResponseEntity<Review> update(@Valid @RequestBody Review review) {
        return ResponseEntity.ok(reviewService.update(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Collection<Review>> findAll(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(reviewService.findAll(filmId, count));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> addLike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        reviewService.addLike(id, userId);
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> addDislike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        reviewService.addDislike(id, userId);
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        reviewService.removeLike(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> removeDislike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        reviewService.removeDislike(id, userId);
        return ResponseEntity.noContent().build();
    }
}