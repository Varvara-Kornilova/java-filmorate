package ru.yandex.practicum.filmorate.service.user;

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
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;
    private final EventService eventService; // сервис событий

    public UserService(UserStorage userStorage,
                       FriendshipStorage friendshipStorage,
                       EventService eventService) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
        this.eventService = eventService;
    }

    public Collection<User> getAllUsers() {
        return userStorage.findAll();
    }

    public User registerUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя не указано, используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User modifyUser(User updatedUser) {
        if (updatedUser.getId() == null) {
            log.warn("Попытка обновления пользователя без ID");
            throw new ValidationException("Идентификатор пользователя должен быть указан");
        }

        User existingUser = userStorage.findById(updatedUser.getId())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %d не найден", updatedUser.getId())));

        if (updatedUser.getName() == null || updatedUser.getName().isBlank()) {
            updatedUser.setName(updatedUser.getLogin());
        }

        return userStorage.update(updatedUser);
    }

    public User getUserById(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %d не найден", userId)));
    }

    public void deleteUser(Long userId) {
        if (!userStorage.contains(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        userStorage.delete(userId);
    }

    public void sendFriendRequest(Long userId, Long friendId) {
        validateUsersExist(userId, friendId);

        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может быть другом сам себе");
        }

        friendshipStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);

        // записываем событие добавления в друзья
        eventService.addEvent(userId, EventType.FRIEND, EventOperation.ADD, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userStorage.findById(userId).isPresent()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!userStorage.findById(friendId).isPresent()) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        friendshipStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);

        // записываем событие удаления из друзей
        eventService.addEvent(userId, EventType.FRIEND, EventOperation.REMOVE, friendId);
    }

    public Collection<User> getFriendsList(Long userId) {
        if (!userStorage.findById(userId).isPresent()) {
            throw new NotFoundException(
                    String.format("Пользователь с идентификатором %d не найден", userId));
        }
        return friendshipStorage.getFriends(userId);
    }

    public Collection<User> getMutualFriends(Long userId, Long otherUserId) {
        validateUsersExist(userId, otherUserId);
        return friendshipStorage.getCommonFriends(userId, otherUserId);
    }

    public Collection<Film> getRecommendations(Long userId) {
        getUserById(userId);

        Collection<Film> recommendations = userStorage.getRecommendations(userId);

        log.info("Найдено {} рекомендаций для пользователя {}", recommendations.size(), userId);

        return recommendations;
    }

    private void validateUsersExist(Long userId, Long friendId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %d не найден", userId)));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %d не найден", friendId)));
    }
}
