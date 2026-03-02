package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

/**
 * Контроллер для управления пользователями и дружбой.
 * Обрабатывает запросы к эндпоинтам /users.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Возвращает список всех зарегистрированных пользователей.
     */
    @GetMapping
    public Collection<User> listAllUsers() {
        log.info("Запрошен список всех пользователей");
        return userService.getAllUsers();
    }

    /**
     * Возвращает пользователя по идентификатору.
     */
    @GetMapping("/{id}")
    public User fetchUserById(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long id) {
        log.debug("Запрос пользователя с id={}", id);
        return userService.getUserById(id);
    }

    /**
     * Возвращает список друзей указанного пользователя.
     */
    @GetMapping("/{id}/friends")
    public Collection<User> listUserFriends(@PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        log.info("Запрошены друзья пользователя с id={}", id);
        return userService.getFriendsList(id);
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     */
    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> listMutualFriends(
            @PathVariable @Positive(message = "ID первого пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID второго пользователя должен быть положительным") Long otherId) {
        log.info("Запрошены общие друзья пользователей {} и {}", id, otherId);
        return userService.getMutualFriends(id, otherId);
    }

    /**
     * Регистрирует нового пользователя.
     */
    @PostMapping
    public User registerNewUser(@Valid @RequestBody User user) {
        log.info("Регистрация нового пользователя: login={}", user.getLogin());
        return userService.registerUser(user);
    }

    /**
     * Обновляет данные существующего пользователя.
     */
    @PutMapping
    public User modifyUserProfile(@Valid @RequestBody User updatedUser) {
        log.info("Обновление профиля пользователя с id={}", updatedUser.getId());
        return userService.modifyUser(updatedUser);
    }

    /**
     * Добавляет пользователя в друзья (односторонняя подписка).
     * @return пользователь, чей список друзей обновлён
     */
    @PutMapping("/{id}/friends/{friendId}")
    public User initiateFriendship(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным") Long friendId) {
        log.info("Пользователь {} отправляет запрос дружбы пользователю {}", id, friendId);
        userService.sendFriendRequest(id, friendId);
        return userService.getUserById(id);
    }

    /**
     * Удаляет пользователя из списка друзей.
     * @return пользователь, чей список друзей обновлён
     */
    @DeleteMapping("/{id}/friends/{friendId}")
    public User terminateFriendship(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным") Long friendId) {
        log.info("Пользователь {} удаляет пользователя {} из друзей", id, friendId);
        userService.removeFriend(id, friendId);
        return userService.getUserById(id);
    }

    /**
     * Удаляет пользователя по идентификатору.
     */
    @DeleteMapping("/{id}")
    public void excludeUser(@PathVariable @Positive(message = "ID должен быть положительным") Long id) {
        log.info("Удаление пользователя с id={}", id);
        // Если метод есть в сервисе
        // userService.deleteUser(id);
    }
}
