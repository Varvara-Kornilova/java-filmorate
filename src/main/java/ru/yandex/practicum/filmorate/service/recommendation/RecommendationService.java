package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;

import java.util.*;

@Slf4j
@Service
public class RecommendationService {

    private final LikeStorage likeStorage;
    private final FilmStorage filmStorage;

    public RecommendationService(LikeStorage likeStorage, FilmStorage filmStorage) {
        this.likeStorage = likeStorage;
        this.filmStorage = filmStorage;
    }

    public Collection<Film> getRecommendations(Long userId) {
        log.info("Временная реализация рекомендаций для userId: {}", userId);
        // TODO: реализовать полноценный алгоритм рекомендаций
        return Collections.emptyList();
    }
}
