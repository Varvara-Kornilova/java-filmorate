package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;

@Component("userDbStorage")
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    public UserDbStorage(JdbcTemplate jdbc) {
        super(jdbc, new UserRowMapper());
    }

    @Override
    public List<User> findAllUsers() {
        return getAll("SELECT user_id, email, login, name, birthday FROM users");
    }

    @Override
    public User create(User user) {
        String query = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        Long id = insert(query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );

        user.setId(id);
        return user;
    }

    @Override
    public User update(User newUser) {
        Optional<User> existing = findUserById(newUser.getId());
        if (existing.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        String query = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbc.update(query,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday(),
                newUser.getId()
        );

        return newUser;
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return get("SELECT user_id, email, login, name, birthday FROM users WHERE user_id = ?", id);
    }
}
