package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;
    private final EventService eventService;

    public Collection<User> getAllUsers() {
        log.debug("Запрошен список всех пользователей");
        Collection<User> users = userStorage.findAll();
        log.debug("Найдено {} пользователей", users.size());
        return users;
    }

    public User registerUser(User user) {
        log.debug("Регистрация пользователя: login='{}', email='{}'", user.getLogin(), user.getEmail());

        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано, используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        User created = userStorage.create(user);
        log.info("Пользователь зарегистрирован: id={}, login='{}'", created.getId(), created.getLogin());
        return created;
    }

    public User modifyUser(User updatedUser) {
        log.debug("Обновление пользователя: id={}", updatedUser.getId());

        if (updatedUser.getId() == null) {
            log.warn("Попытка обновления пользователя без ID");
            throw new ValidationException("Идентификатор пользователя должен быть указан");
        }

        User existingUser = userStorage.findById(updatedUser.getId())
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден при обновлении", updatedUser.getId());
                    return new NotFoundException(
                            String.format("Пользователь с идентификатором %d не найден", updatedUser.getId()));
                });

        if (updatedUser.getName() == null || updatedUser.getName().isBlank()) {
            log.debug("Имя в обновлении пустое, будет установлено значение логина: {}", updatedUser.getLogin());
            updatedUser.setName(updatedUser.getLogin());
        }

        User result = userStorage.update(updatedUser);
        log.info("Пользователь обновлён: id={}, login='{}'", result.getId(), result.getLogin());
        return result;
    }

    public User getUserById(Long userId) {
        log.debug("Поиск пользователя по id={}", userId);
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new NotFoundException(
                            String.format("Пользователь с идентификатором %d не найден", userId));
                });
    }

    public void deleteUser(Long userId) {
        log.debug("Удаление пользователя с id={}", userId);

        if (!userStorage.contains(userId)) {
            log.warn("Попытка удаления несуществующего пользователя с id={}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        userStorage.delete(userId);
        log.info("Пользователь с id={} успешно удалён", userId);
    }

    public void sendFriendRequest(Long userId, Long friendId) {
        log.debug("Запрос дружбы: userId={}, friendId={}", userId, friendId);
        validateUsersExist(userId, friendId);

        if (userId.equals(friendId)) {
            log.warn("Пользователь id={} попытался добавить в друзья самого себя", userId);
            throw new ValidationException("Пользователь не может быть другом сам себе");
        }

        friendshipStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);

        eventService.addEvent(userId, EventType.FRIEND, EventOperation.ADD, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.debug("Удаление из друзей: userId={}, friendId={}", userId, friendId);

        if (!userStorage.findById(userId).isPresent()) {
            log.warn("Пользователь с id={} не найден при удалении друга", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!userStorage.findById(friendId).isPresent()) {
            log.warn("Пользователь с id={} не найден при удалении из друзей", friendId);
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        friendshipStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);

        eventService.addEvent(userId, EventType.FRIEND, EventOperation.REMOVE, friendId);
    }

    public Collection<User> getFriendsList(Long userId) {
        log.debug("Запрос списка друзей пользователя id={}", userId);

        if (!userStorage.findById(userId).isPresent()) {
            log.warn("Пользователь с id={} не найден при запросе друзей", userId);
            throw new NotFoundException(
                    String.format("Пользователь с идентификатором %d не найден", userId));
        }

        Collection<User> friends = friendshipStorage.getFriends(userId);
        log.debug("Найдено {} друзей у пользователя {}", friends.size(), userId);
        return friends;
    }

    public Collection<User> getMutualFriends(Long userId, Long otherUserId) {
        log.debug("Запрос общих друзей: userId={}, otherUserId={}", userId, otherUserId);
        validateUsersExist(userId, otherUserId);

        Collection<User> mutual = friendshipStorage.getCommonFriends(userId, otherUserId);
        log.debug("Найдено {} общих друзей у пользователей {} и {}", mutual.size(), userId, otherUserId);
        return mutual;
    }

    public Collection<Film> getRecommendations(Long userId) {
        log.debug("Запрос рекомендаций для пользователя id={}", userId);
        getUserById(userId);

        Collection<Film> recommendations = userStorage.getRecommendations(userId);
        log.info("Найдено {} рекомендаций для пользователя {}", recommendations.size(), userId);
        return recommendations;
    }

    private void validateUsersExist(Long userId, Long friendId) {
        log.debug("Валидация существования пользователей: userId={}, friendId={}", userId, friendId);
        userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден при валидации", userId);
                    return new NotFoundException(
                            String.format("Пользователь с идентификатором %d не найден", userId));
                });
        userStorage.findById(friendId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден при валидации", friendId);
                    return new NotFoundException(
                            String.format("Пользователь с идентификатором %d не найден", friendId));
                });
    }
}
