package ru.yandex.practicum.filmorate.storage.like;

import java.util.Map;
import java.util.Set;

public interface LikeStorage {

    Map<Long, Set<Long>> getAllUserLikes();
}
