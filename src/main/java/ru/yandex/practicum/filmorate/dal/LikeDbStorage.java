package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
public class LikeDbStorage implements LikeStorage {

    private final JdbcTemplate jdbcTemplate;

    public LikeDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

        return userLikes;
    }
}
