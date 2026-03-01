package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;

import java.util.List;

@Component
public class FriendshipDbStorage extends BaseDbStorage<User> implements FriendshipStorage {

    public FriendshipDbStorage(JdbcTemplate jdbc) {
        super(jdbc, new UserRowMapper());
    }

    @Override
    public void addFriendship(Long userId, Long friendId, String statusName) {
        Long statusId = jdbc.queryForObject(
                "SELECT status_id FROM friendship_status WHERE name = ?",
                Long.class, statusName);

        if (statusId == null) {
            throw new RuntimeException("Статус дружбы не найден: " + statusName);
        }

        String sql = """
            MERGE INTO friendship (user_id, friend_id, status_id) 
            KEY (user_id, friend_id) 
            VALUES (?, ?, ?)
            """;
        jdbc.update(sql, userId, friendId, statusId);
    }

    @Override
    public void removeFriendship(Long userId, Long friendId) {
        delete("DELETE FROM friendship WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        String sql = """
            SELECT u.user_id, u.email, u.login, u.name, u.birthday
            FROM friendship f
            JOIN users u ON f.friend_id = u.user_id
            WHERE f.user_id = ?
            """;
        return getAll(sql, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        String sql = """
            SELECT u.user_id, u.email, u.login, u.name, u.birthday
            FROM friendship f1
            JOIN friendship f2 ON f1.friend_id = f2.friend_id
            JOIN users u ON f1.friend_id = u.user_id
            WHERE f1.user_id = ? AND f2.user_id = ?
            """;
        return getAll(sql, userId, otherUserId);
    }

    @Override
    public boolean exists(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }
}
