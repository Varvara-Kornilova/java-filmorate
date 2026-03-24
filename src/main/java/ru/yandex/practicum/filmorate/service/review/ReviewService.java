package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.event.EventService;
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
    private final EventService eventService;

    public Review create(Review review) {
        log.debug("Создание отзыва: userId={}, filmId={}", review.getUserId(), review.getFilmId());
        checkUserExists(review.getUserId());
        checkFilmExists(review.getFilmId());

        Review createdReview = reviewStorage.create(review);
        log.info("Отзыв создан: id={}, userId={}, filmId={}",
                createdReview.getReviewId(), createdReview.getUserId(), createdReview.getFilmId());

        eventService.addEvent(
                createdReview.getUserId(),
                EventType.REVIEW,
                EventOperation.ADD,
                createdReview.getReviewId()
        );

        return createdReview;
    }

    public Review update(Review review) {
        log.debug("Обновление отзыва: id={}", review.getReviewId());

        Review oldReview = getByIdOrThrow(review.getReviewId());

        review.setUserId(oldReview.getUserId());
        review.setFilmId(oldReview.getFilmId());

        Review updatedReview = reviewStorage.update(review);
        log.info("Отзыв обновлён: id={}, userId={}, filmId={}",
                updatedReview.getReviewId(), updatedReview.getUserId(), updatedReview.getFilmId());

        eventService.addEvent(
                updatedReview.getUserId(),
                EventType.REVIEW,
                EventOperation.UPDATE,
                updatedReview.getReviewId()
        );

        return updatedReview;
    }

    public void delete(Long id) {
        log.debug("Удаление отзыва с id={}", id);

        Review review = getByIdOrThrow(id);

        reviewStorage.delete(id);
        log.info("Удален отзыв с ID: {}", id);

        eventService.addEvent(
                review.getUserId(),
                EventType.REVIEW,
                EventOperation.REMOVE,
                review.getReviewId()
        );
    }

    public Review findById(Long id) {
        log.debug("Поиск отзыва по id={}", id);
        return getByIdOrThrow(id);
    }

    public Collection<Review> findAll(Long filmId, int count) {
        Collection<Review> reviews = reviewStorage.findAll(filmId, count);
        log.debug("Найдено {} отзывов", reviews.size());
        return reviews;
    }

    public void addLike(Long reviewId, Long userId) {
        log.debug("Постановка лайка: userId={}, reviewId={}", userId, reviewId);
        getByIdOrThrow(reviewId);
        checkUserExists(userId);
        reviewStorage.addLike(reviewId, userId);
        log.info("Пользователь {} поставил лайк отзыву {}", userId, reviewId);
    }

    public void addDislike(Long reviewId, Long userId) {
        log.debug("Постановка дизлайка: userId={}, reviewId={}", userId, reviewId);
        getByIdOrThrow(reviewId);
        checkUserExists(userId);
        reviewStorage.addDislike(reviewId, userId);
        log.info("Пользователь {} поставил дизлайк отзыву {}", userId, reviewId);
    }

    public void removeLike(Long reviewId, Long userId) {
        log.debug("Удаление лайка: userId={}, reviewId={}", userId, reviewId);
        getByIdOrThrow(reviewId);
        checkUserExists(userId);
        reviewStorage.removeLike(reviewId, userId);
        log.info("Пользователь {} удалил лайк у отзыва {}", userId, reviewId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        log.debug("Удаление дизлайка: userId={}, reviewId={}", userId, reviewId);
        getByIdOrThrow(reviewId);
        checkUserExists(userId);
        reviewStorage.removeDislike(reviewId, userId);
        log.info("Пользователь {} удалил дизлайк у отзыва {}", userId, reviewId);
    }

    private void checkUserExists(Long userId) {
        log.debug("Проверка существования пользователя с id={}", userId);

        if (!userStorage.contains(userId)) {
            log.warn("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    private void checkFilmExists(Long filmId) {
        log.debug("Проверка существования фильма с id={}", filmId);

        if (!filmStorage.contains(filmId)) {
            log.warn("Фильм с id={} не найден", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }

    public Review getByIdOrThrow(Long id) {
        log.debug("Поиск отзыва по id={}", id);
        return reviewStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Отзыв с id={} не найден", id);
                    return new NotFoundException("Отзыв с ID " + id + " не найден");
                });
    }
}