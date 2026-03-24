package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
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
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final EventService eventService;

    @GetMapping
    public Collection<User> listAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User fetchUserById(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> listUserFriends(@PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        return userService.getFriendsList(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> listMutualFriends(
            @PathVariable @Positive(message = "ID первого пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID второго пользователя должен быть положительным") Long otherId) {
        return userService.getMutualFriends(id, otherId);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getUserFeed(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        return eventService.getUserFeed(id);
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        return userService.getRecommendations(id);
    }

    @PostMapping
    public User registerNewUser(@Valid @RequestBody User user) {
        return userService.registerUser(user);
    }

    @PutMapping
    public User modifyUserProfile(@Valid @RequestBody User updatedUser) {
        return userService.modifyUser(updatedUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User initiateFriendship(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным") Long friendId) {
        userService.sendFriendRequest(id, friendId);
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User terminateFriendship(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным") Long friendId) {
        userService.removeFriend(id, friendId);
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable @Positive(message = "ID должен быть положительным") Long id) {
        userService.deleteUser(id);
    }
}