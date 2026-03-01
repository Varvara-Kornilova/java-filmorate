package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;
import java.util.Optional;

public interface FriendshipStorage {
    /** Добавить заявку в друзья (односторонняя) */
    void addFriendship(Long userId, Long friendId, String statusName);

    /** Удалить заявку в друзья */
    void removeFriendship(Long userId, Long friendId);

    /** Получить список друзей пользователя */
    List<User> getFriends(Long userId);

    /** Получить список общих друзей */
    List<User> getCommonFriends(Long userId, Long otherUserId);

    /** Проверить, существует ли связь */
    boolean exists(Long userId, Long friendId);
}
