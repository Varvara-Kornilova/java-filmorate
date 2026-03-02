package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

/**
 * Сервис для управления пользователями и дружбой.
 */
@Slf4j
@Service
@Transactional
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public UserService(UserStorage userStorage, FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    /**
     * Возвращает список всех пользователей.
     */
    public Collection<User> getAllUsers() {
        return userStorage.findAll();
    }

    /**
     * Создаёт нового пользователя.
     * Если имя не указано, используется логин.
     */
    public User registerUser(User user) {
        // Если имя пустое — используем логин
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя не указано, используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    /**
     * Обновляет данные пользователя.
     */
    public User modifyUser(User updatedUser) {
        if (updatedUser.getId() == null) {
            log.warn("Попытка обновления пользователя без ID");
            throw new ValidationException("Идентификатор пользователя должен быть указан");
        }

        User existingUser = userStorage.findById(updatedUser.getId())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %d не найден", updatedUser.getId())));

        // Если имя пустое — используем логин
        if (updatedUser.getName() == null || updatedUser.getName().isBlank()) {
            updatedUser.setName(updatedUser.getLogin());
        }

        return userStorage.update(updatedUser);
    }

    /**
     * Находит пользователя по идентификатору.
     */
    public User getUserById(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %d не найден", userId)));
    }

    /**
     * Добавляет пользователя в друзья (односторонняя подписка).
     */
    public void sendFriendRequest(Long userId, Long friendId) {
        validateUsersExist(userId, friendId);

        if (userId.equals(friendId)) {
            log.warn("Пользователь {} пытается добавить себя в друзья", userId);
            throw new ValidationException("Пользователь не может быть другом сам себе");
        }

        friendshipStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    /**
     * Удаляет пользователя из друзей.
     */
    public void removeFriend(Long userId, Long friendId) {
        validateUsersExist(userId, friendId);

        friendshipStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    /**
     * Возвращает список друзей пользователя.
     */
    public Collection<User> getFriendsList(Long userId) {
        if (!userStorage.findById(userId).isPresent()) {
            throw new NotFoundException(
                    String.format("Пользователь с идентификатором %d не найден", userId));
        }
        return friendshipStorage.getFriends(userId);
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     */
    public Collection<User> getMutualFriends(Long userId, Long otherUserId) {
        validateUsersExist(userId, otherUserId);
        return friendshipStorage.getCommonFriends(userId, otherUserId);
    }

    /**
     * Проверяет существование обоих пользователей.
     */
    private void validateUsersExist(Long userId, Long friendId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %d не найден", userId)));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %d не найден", friendId)));
    }
}
