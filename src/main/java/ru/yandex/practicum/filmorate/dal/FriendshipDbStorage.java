package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;

import java.util.*;

@Repository
public class FriendshipDbStorage extends BaseDbStorage<User> implements FriendshipStorage {

    private static final String ADD_FRIEND = """
            INSERT INTO friendship (user_id, friend_id, status_id)
            VALUES (?, ?, 2)
            """;

    private static final String REMOVE_FRIEND = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRIENDS_IDS = "SELECT friend_id FROM friendship WHERE user_id = ?";

    private static final String GET_COMMON_FRIENDS_IDS = """
            SELECT f1.friend_id
            FROM friendship f1
            JOIN friendship f2 ON f1.friend_id = f2.friend_id
            WHERE f1.user_id = ? AND f2.user_id = ?
            """;

    private static final String GET_USERS_BY_IDS = "SELECT * FROM users WHERE user_id IN (:ids)";

    private static final String CHECK_FRIENDSHIP = """
            SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public FriendshipDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        super(jdbcTemplate, userRowMapper);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {

        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может быть другом сам себе");
        }

        Integer count = jdbcTemplate.queryForObject(CHECK_FRIENDSHIP, Integer.class, userId, friendId);

        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.update(ADD_FRIEND, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbcTemplate.update(REMOVE_FRIEND, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        List<Long> friendIds = jdbcTemplate.queryForList(GET_FRIENDS_IDS, Long.class, userId);

        if (friendIds.isEmpty()) {
            return Collections.emptyList();
        }

        return getUsersByIds(friendIds);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        List<Long> commonIds = jdbcTemplate.queryForList(GET_COMMON_FRIENDS_IDS, Long.class, userId, otherUserId);

        if (commonIds.isEmpty()) {
            return Collections.emptyList();
        }

        return getUsersByIds(commonIds);
    }

    private Collection<User> getUsersByIds(List<Long> ids) {
        Map<String, Object> params = Collections.singletonMap("ids", ids);
        return namedParameterJdbcTemplate.query(GET_USERS_BY_IDS, params, rowMapper);
    }
}