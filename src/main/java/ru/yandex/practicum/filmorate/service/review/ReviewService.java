package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review create(Review review) {
        checkUserExists(review.getUserId());
        checkFilmExists(review.getFilmId());

        Review createdReview = reviewStorage.create(review);
        log.info("Создан отзыв с ID: {}", createdReview.getReviewId());
        return createdReview;
    }

    public Review update(Review review) {
        Review oldReview = getByIdOrThrow(review.getReviewId());

        review.setUserId(oldReview.getUserId());
        review.setFilmId(oldReview.getFilmId());

        Review updatedReview = reviewStorage.update(review);
        log.info("Обновлен отзыв с ID: {}", updatedReview.getReviewId());
        return updatedReview;
    }

    public void delete(Long id) {
        getByIdOrThrow(id);
        reviewStorage.delete(id);
        log.info("Удален отзыв с ID: {}", id);
    }

    public Review findById(Long id) {
        return getByIdOrThrow(id);
    }

    public Collection<Review> findAll(Long filmId, int count) {
        return reviewStorage.findAll(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        getByIdOrThrow(reviewId);
        checkUserExists(userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        getByIdOrThrow(reviewId);
        checkUserExists(userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        getByIdOrThrow(reviewId);
        checkUserExists(userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        getByIdOrThrow(reviewId);
        checkUserExists(userId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void checkUserExists(Long userId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    private void checkFilmExists(Long filmId) {
        if (!filmStorage.contains(filmId)) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }

    public Review getByIdOrThrow(Long id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + id + " не найден"));
    }
}

