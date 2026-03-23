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
        log.info("=== getAllUserLikes() вызван ===");
        try {
            String sql = "SELECT user_id, film_id FROM likes ORDER BY user_id, film_id";
            log.debug("Выполняем SQL: {}", sql);

            Map<Long, Set<Long>> userLikes = new HashMap<>();

            jdbcTemplate.query(sql, rs -> {
                Long userId = rs.getLong("user_id");
                Long filmId = rs.getLong("film_id");
                log.debug("Найдена запись: user={}, film={}", userId, filmId);
                userLikes.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
            });

            log.info("getAllUserLikes() вернул {} пользователей", userLikes.size());
            return userLikes;

        } catch (Exception e) {
            log.error("Ошибка в getAllUserLikes(): {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при получении лайков", e);
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка: film={}, user={}", filmId, userId);
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Лайк успешно добавлен");
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка: film={}, user={}", filmId, userId);
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Лайк успешно удалён");
    }

    @Override
    public Set<Long> getUserLikes(Long userId) {
        log.info("Получение лайков пользователя {}", userId);
        String sql = "SELECT film_id FROM likes WHERE user_id = ?";
        List<Long> filmIds = jdbcTemplate.queryForList(sql, Long.class, userId);
        log.debug("Пользователь {} имеет {} лайков", userId, filmIds.size());
        return new HashSet<>(filmIds);
    }

    @Override
    public boolean hasLike(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }
}
