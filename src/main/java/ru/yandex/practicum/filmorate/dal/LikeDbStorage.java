package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;

import java.util.*;

@Slf4j
@Repository
public class LikeDbStorage implements LikeStorage {

    private final JdbcTemplate jdbcTemplate;

    public LikeDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        log.info("LikeDbStorage инициализирован");
    }

    @Override
    public Map<Long, Set<Long>> getAllUserLikes() {
        String sql = "SELECT user_id, film_id FROM likes ORDER BY user_id, film_id";

        Map<Long, Set<Long>> userLikes = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long userId = rs.getLong("user_id");
            Long filmId = rs.getLong("film_id");
            userLikes.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
        });

        log.debug("getAllUserLikes() вернул {} пользователей", userLikes.size());
        return userLikes;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Добавлен лайк: film={}, user={}", filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Удалён лайк: film={}, user={}", filmId, userId);
    }

    @Override
    public Set<Long> getUserLikes(Long userId) {
        String sql = "SELECT film_id FROM likes WHERE user_id = ?";
        List<Long> filmIds = jdbcTemplate.queryForList(sql, Long.class, userId);
        return new HashSet<>(filmIds);
    }

    @Override
    public boolean hasLike(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }
}
