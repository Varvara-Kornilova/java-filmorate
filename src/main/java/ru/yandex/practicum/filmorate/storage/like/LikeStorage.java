package ru.yandex.practicum.filmorate.storage.like;

import java.util.Map;
import java.util.Set;

public interface LikeStorage {

    Map<Long, Set<Long>> getAllUserLikes();

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    Set<Long> getUserLikes(Long userId);

    boolean hasLike(Long filmId, Long userId);
}
