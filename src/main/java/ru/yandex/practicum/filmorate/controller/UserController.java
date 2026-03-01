package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserStorage userStorage;
    private final UserService userService;

    public UserController(@Qualifier("userDbStorage") UserStorage userStorage,
                          UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public List<User> findAllUsers() {
        log.debug("Запрос на получение всех пользователей");
        List<User> users = userStorage.findAllUsers();
        log.debug("Найдено {} пользователей", users.size());
        return users;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable("id") Long id) {
        log.debug("Запрос на получение пользователя с id = {}", id);
        User user = userStorage.findUserById(id)
                .orElseThrow(() -> new ru.yandex.practicum.filmorate.exception.NotFoundException(
                        "Пользователь с id = " + id + " не найден"));
        log.debug("Найден пользователь: {} (id = {})", user.getName() != null ? user.getName() : user.getLogin(), user.getId());
        return user;
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable("id") Long userId) {
        log.debug("Запрос на получение друзей пользователя с id = {}", userId);
        List<User> friends = userService.getFriends(userId);
        log.debug("У пользователя {} {} друзей", userId, friends.size());
        return friends;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable("id") Long userId, @PathVariable("otherId") Long otherUserId) {
        log.debug("Запрос на получение общих друзей между {} и {}", userId, otherUserId);
        List<User> commonFriends = userService.getCommonFriends(userId, otherUserId);
        log.debug("У пользователей {} и {} {} общих друзей", userId, otherUserId, commonFriends.size());
        return commonFriends;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Создание нового пользователя: логин = {}", user.getLogin());
        User createdUser = userStorage.create(user);
        log.info("Пользователь успешно создан с id = {}", createdUser.getId());
        return createdUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Обновление пользователя с id = {}", newUser.getId());
        User updatedUser = userStorage.update(newUser);
        log.info("Пользователь с id = {} успешно обновлён", updatedUser.getId());
        return updatedUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        log.info("Пользователь {} добавляет друга {}", userId, friendId);
        User user = userService.addFriend(userId, friendId);
        log.info("Дружба установлена: пользователи {} и {} теперь друзья", userId, friendId);
        return user;
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        log.info("Пользователь {} удаляет друга {}", userId, friendId);
        User user = userService.removeFriend(userId, friendId);
        log.info("Дружба удалена: пользователи {} и {} больше не друзья", userId, friendId);
        return user;
    }
}
