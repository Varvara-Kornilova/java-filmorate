package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;

public interface FriendshipStorage {
    // Добавление друга (подписка)
    void addFriend(Long userId, Long friendId);

    // Удаление друга
    void removeFriend(Long userId, Long friendId);

    // Список друзей
    Collection<User> getFriends(Long userId);

    // Общие друзья
    Collection<User> getCommonFriends(Long userId, Long otherUserId);
}
