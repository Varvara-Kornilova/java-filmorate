package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage {

    public ReviewDbStorage(JdbcTemplate jdbcTemplate, ReviewRowMapper mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)";
        Long id = insertAndGetId(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId());

        review.setReviewId(id);
        return findById(id).orElseThrow(() -> new NotFoundException("Ошибка при создании отзыва"));
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        executeUpdate(sql, review.getContent(), review.getIsPositive(), review.getReviewId());

        return findById(review.getReviewId()).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    @Override
    public Optional<Review> findById(Long id) {
        String sql = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, " +
                "COALESCE(" +
                "(SELECT COUNT(CASE WHEN is_like = true THEN 1 END) - " +
                " COUNT(CASE WHEN is_like = false THEN 1 END) " +
                " FROM review_likes " +
                " WHERE review_id = r.review_id), 0) AS useful " +
                "FROM reviews r " +
                "WHERE r.review_id = ?";

        return findOptional(sql, id);
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        executeUpdate(sql, id);
    }

    @Override
    public Collection<Review> findAll(Long filmId, int count) {
        String baseSql = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, " +
                "COALESCE(" +
                "(SELECT COUNT(CASE WHEN is_like = true THEN 1 END) - " +
                "        COUNT(CASE WHEN is_like = false THEN 1 END) " +
                " FROM review_likes " +
                " WHERE review_id = r.review_id), 0) AS useful " +
                "FROM reviews r ";
        if (filmId == null) {
            String sql = baseSql + " ORDER BY useful DESC LIMIT ?";
            return queryForList(sql, count);
        } else {
            String sql = baseSql + " WHERE r.film_id = ? ORDER BY useful DESC LIMIT ?";
            return queryForList(sql, filmId, count);
        }
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        String sql = "MERGE INTO review_likes (review_id, user_id, is_like) KEY(review_id, user_id) VALUES (?, ?, true)";
        executeUpdate(sql, reviewId, userId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        String sql = "MERGE INTO review_likes (review_id, user_id, is_like) KEY(review_id, user_id) VALUES (?, ?, false)";
        executeUpdate(sql, reviewId, userId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = true";
        executeUpdate(sql, reviewId, userId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = false";
        executeUpdate(sql, reviewId, userId);
    }

    @Override
    public boolean contains(Long id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reviews WHERE review_id = ?)";
        return exists(sql, id);
    }

}