package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.Collection;
import java.util.List;

/**
 * Контроллер для управления пользователями и дружбой.
 * Обрабатывает запросы к эндпоинтам /users.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final EventService eventService;

    @GetMapping
    public Collection<User> listAllUsers() {
        log.info("Запрошен список всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User fetchUserById(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long id) {
        log.debug("Запрос пользователя с id={}", id);
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> listUserFriends(@PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        log.info("Запрошены друзья пользователя с id={}", id);
        return userService.getFriendsList(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> listMutualFriends(
            @PathVariable @Positive(message = "ID первого пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID второго пользователя должен быть положительным") Long otherId) {
        log.info("Запрошены общие друзья пользователей {} и {}", id, otherId);
        return userService.getMutualFriends(id, otherId);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getUserFeed(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        log.info("Запрошена лента событий пользователя с id={}", id);
        return eventService.getUserFeed(id);
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        log.info("Запрошены рекомендации для пользователя {}", id);
        return userService.getRecommendations(id);
    }

    @PostMapping
    public User registerNewUser(@Valid @RequestBody User user) {
        log.info("Регистрация нового пользователя: login={}", user.getLogin());
        return userService.registerUser(user);
    }

    @PutMapping
    public User modifyUserProfile(@Valid @RequestBody User updatedUser) {
        log.info("Обновление профиля пользователя с id={}", updatedUser.getId());
        return userService.modifyUser(updatedUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User initiateFriendship(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным") Long friendId) {
        log.info("Пользователь {} отправляет запрос дружбы пользователю {}", id, friendId);
        userService.sendFriendRequest(id, friendId);
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User terminateFriendship(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным") Long friendId) {
        log.info("Пользователь {} удаляет пользователя {} из друзей", id, friendId);
        userService.removeFriend(id, friendId);
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable @Positive(message = "ID должен быть положительным") Long id) {
        log.info("Удаление пользователя с id={}", id);
        userService.deleteUser(id);
    }
}
