package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.EntityValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final EntityValidator validator;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       EntityValidator validator) {
        this.userStorage = userStorage;
        this.validator = validator;
    }

    public User addFriend(Long userId, Long friendId) {
        log.debug("Добавление дружбы: пользователь {} добавляет {}", userId, friendId);

        User user = validator.getUserOrThrow(userId);
        User friend = validator.getUserOrThrow(friendId);

        if (userId.equals(friendId)) {
            log.warn("Попытка добавить себя в друзья: пользователь {}", userId);
            throw new DuplicateDataException("Пользователь не может добавить себя в друзья");
        }

        boolean userAddedFriend = user.getFriends().add(friendId);
        boolean friendAddedUser = friend.getFriends().add(userId);

        if (userAddedFriend && friendAddedUser) {
            log.info("Дружба установлена: пользователи {} и {} теперь друзья", userId, friendId);
        } else {
            log.debug("Дружба уже существует между пользователями {} и {}", userId, friendId);
        }

        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        log.debug("Удаление дружбы: пользователь {} удаляет {}", userId, friendId);

        User user = validator.getUserOrThrow(userId);
        User friend = validator.getUserOrThrow(friendId);

        if (userId.equals(friendId)) {
            log.warn("Попытка удалить себя из друзей: пользователь {}", userId);
            throw new DuplicateDataException("Пользователь не может удалить себя из друзей");
        }

        boolean removedFromUser = user.getFriends().remove(friendId);
        boolean removedFromFriend = friend.getFriends().remove(userId);

        if (removedFromUser && removedFromFriend) {
            log.info("Дружба удалена: пользователи {} и {} больше не друзья", userId, friendId);
        } else {
            log.debug("Попытка удалить несуществующую дружбу между {} и {}", userId, friendId);
        }

        return user;
    }

    public List<User> getFriends(Long userId) {
        log.debug("Запрос на получение друзей пользователя {}", userId);

        User user = validator.getUserOrThrow(userId);

        List<User> friends = user.getFriends().stream()
                .map(friendId -> userStorage.findUserById(friendId)
                        .orElseThrow(() -> new NotFoundException(
                                "Друг с id = " + friendId + " не найден")))
                .toList();

        log.debug("У пользователя {} {} друзей", userId, friends.size());
        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        log.debug("Запрос на получение общих друзей: {} и {}", userId, otherUserId);

        User user = validator.getUserOrThrow(userId);
        User otherUser = validator.getUserOrThrow(otherUserId);

        Set<Long> commonFriendIds = new HashSet<>(user.getFriends());
        commonFriendIds.retainAll(otherUser.getFriends());

        List<User> commonFriends = commonFriendIds.stream()
                .map(friendId -> userStorage.findUserById(friendId)
                        .orElseThrow(() -> new NotFoundException(
                                "Друг с id = " + friendId + " не найден")))
                .toList();

        log.debug("У пользователей {} и {} {} общих друзей", userId, otherUserId, commonFriends.size());
        return commonFriends;
    }
}
