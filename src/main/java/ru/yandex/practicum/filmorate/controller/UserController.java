package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAllUsers() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validateUser(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {

        if (!users.containsKey(newUser.getId())) {
            log.warn("Введен несуществующий id пользователя: {}", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        User oldUser = users.get(newUser.getId());

        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            oldUser.setEmail(newUser.getEmail());
        }

        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            oldUser.setLogin(newUser.getLogin());
        }

        if (newUser.getName() != null && !newUser.getName().isEmpty()) {
            oldUser.setName(newUser.getName());
        }

        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }

        validateUser(oldUser);
        log.info("Обновлены данные о пользователе c id {}: {}", newUser.getId(), oldUser.getName());
        return oldUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            if (user.getEmail() == null) {
                log.warn("Ошибка валидации: имейл не может быть null");
            } else if (user.getEmail().isBlank()) {
                log.warn("Ошибка валидации: имейл отсутствует");
            } else if (!user.getEmail().contains("@")) {
                log.warn("Ошибка валидации: имейл не содержит обязательный символ @");
            }
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            if (user.getLogin() == null) {
                log.warn("Ошибка валидации: логин не может быть null");
            } else if (user.getLogin().isBlank()) {
                log.warn("Ошибка валидации: логин не может быть пуст");
            } else if (user.getLogin().contains(" ")) {
                log.warn("Ошибка валидации: логин не может содержать пробелы");
            }
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }

        if (user.getName() == null || user.getName().isEmpty()) {
            log.info("Отсутствует имя пользователя, имя пользователя заменено на логин {}", user.getLogin());
            user.setName(user.getLogin());
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("дата рождения не может быть в будущем");
        }
    }
}
