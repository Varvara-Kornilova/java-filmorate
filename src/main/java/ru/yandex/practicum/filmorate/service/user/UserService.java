package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.EntityValidator;

import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;
    private final EntityValidator validator;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       FriendshipStorage friendshipStorage,
                       EntityValidator validator) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
        this.validator = validator;
    }

    public User addFriend(Long userId, Long friendId) {
        log.debug("Добавление дружбы: пользователь {} добавляет {}", userId, friendId);

        validator.getUserOrThrow(userId);
        validator.getUserOrThrow(friendId);

        if (userId.equals(friendId)) {
            throw new DuplicateDataException("Пользователь не может добавить себя в друзья");
        }

        friendshipStorage.addFriendship(userId, friendId, "НЕПОДТВЕРЖДЕННАЯ");

        log.info("Пользователь {} добавил {} в друзья", userId, friendId);

        return userStorage.findUserById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id = " + userId + " не найден")
        );
    }

    public User removeFriend(Long userId, Long friendId) {
        log.debug("Удаление дружбы: пользователь {} удаляет {}", userId, friendId);

        validator.getUserOrThrow(userId);
        validator.getUserOrThrow(friendId);

        if (userId.equals(friendId)) {
            throw new DuplicateDataException("Пользователь не может удалить себя из друзей");
        }

        friendshipStorage.removeFriendship(userId, friendId);

        log.info("Пользователь {} удалил {} из друзей", userId, friendId);
        return userStorage.findUserById(userId).orElseThrow();
    }

    public List<User> getFriends(Long userId) {
        validator.getUserOrThrow(userId);
        return friendshipStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        validator.getUserOrThrow(userId);
        validator.getUserOrThrow(otherUserId);
        return friendshipStorage.getCommonFriends(userId, otherUserId);
    }
}
