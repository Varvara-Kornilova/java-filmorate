package ru.yandex.practicum.filmorate.storage.user;

import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAllUsers() {
        return users.values();
    }

    @Override
    public User create(@Valid User user) {
        user.setId(getNextId());
        useLoginAsNameIfNameIsNotValid(user);
        users.put(user.getId(), user);
        //log.info("Создан пользователь: {}", user);
        return user;
    }

    @Override
    public User update(@Valid User newUser) {
        if (newUser.getId() == null) {
            //log.warn("null ID");
            throw new ValidationException("ID пользователя не может быть пустым при обновлении");
        }
        if (!users.containsKey(newUser.getId())) {
            //log.warn("Введен несуществующий id пользователя: {}", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        useLoginAsNameIfNameIsNotValid(newUser);
        users.put(newUser.getId(), newUser);
        //log.info("Обновлены данные о пользователе c id {}: {}", newUser.getId(), newUser.getName());
        return newUser;
    }

    @Override
    public Optional<User> findUserById(Long id) {
        if (id == null || id < 0) {
            return Optional.empty();
        }

        return Optional.ofNullable(users.get(id));
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public void useLoginAsNameIfNameIsNotValid(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }
}
