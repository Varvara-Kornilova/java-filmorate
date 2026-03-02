package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    private static final String FIND_ALL = "SELECT * FROM users ORDER BY user_id";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT = """
            INSERT INTO users (email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE = """
            UPDATE users SET
            email = ?,
            login = ?,
            name = ?,
            birthday = ?
            WHERE user_id = ?
            """;
    private static final String DELETE = "DELETE FROM users WHERE user_id = ?";
    private static final String EXISTS = "SELECT EXISTS(SELECT 1 FROM users WHERE user_id = ?)";

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        super(jdbcTemplate, userRowMapper);
    }

    @Override
    public Collection<User> findAll() {
        return queryForList(FIND_ALL);
    }

    @Override
    public User create(User user) {
        Long id = insertAndGetId(INSERT,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        executeUpdate(UPDATE,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return findOptional(FIND_BY_ID, id);
    }

    @Override
    public void delete(Long id) {
        executeUpdate(DELETE, id);
    }

    public boolean contains(Long id) {
        return exists(EXISTS, id);
    }
}
