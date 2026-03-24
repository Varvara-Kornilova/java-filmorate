package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Collection<User>> listAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> fetchUserById(
            @PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Collection<User>> listUserFriends(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        return ResponseEntity.ok(userService.getFriendsList(id));
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<Collection<User>> listMutualFriends(
            @PathVariable @Positive(message = "ID первого пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID второго пользователя должен быть положительным") Long otherId) {
        return ResponseEntity.ok(userService.getMutualFriends(id, otherId));
    }

    @GetMapping("/{id}/feed")
    public ResponseEntity<List<Event>> getUserFeed(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        return ResponseEntity.ok(eventService.getUserFeed(id));
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<Collection<Film>> getRecommendations(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        return ResponseEntity.ok(userService.getRecommendations(id));
    }

    @PostMapping
    public ResponseEntity<User> registerNewUser(@Valid @RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registerUser(user));
    }

    @PutMapping
    public ResponseEntity<User> modifyUserProfile(@Valid @RequestBody User updatedUser) {
        return ResponseEntity.ok(userService.modifyUser(updatedUser));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> initiateFriendship(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным") Long friendId) {
        userService.sendFriendRequest(id, friendId);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> terminateFriendship(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным") Long friendId) {
        userService.removeFriend(id, friendId);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Positive(message = "ID должен быть положительным") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}