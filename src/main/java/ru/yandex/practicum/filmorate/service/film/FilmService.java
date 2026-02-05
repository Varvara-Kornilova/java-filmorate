package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.validator.EntityValidator;
import java.util.Comparator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final EntityValidator validator;
    Map<Long, Set<Long>> likes = new ConcurrentHashMap<>();

    public FilmService(FilmStorage filmStorage, EntityValidator validator) {
        this.filmStorage = filmStorage;
        this.validator = validator;
    }

    public Film addLike(Long filmId, Long userId) {
        log.debug("Добавление лайка: фильм {} от пользователя {}", filmId, userId);
        Film film = validateAndGetFilm(filmId, userId);

        boolean isNewLike = likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);

        film.getLikes().add(userId);

        if (isNewLike) {
            log.info("Лайк добавлен: фильм {} получил лайк от пользователя {}", filmId, userId);
        } else {
            log.debug("Лайк уже существует: фильм {} от пользователя {}", filmId, userId);
        }

        return film;
    }

    public Film removeLike(Long filmId, Long userId) {
        log.debug("Удаление лайка: фильм {} от пользователя {}", filmId, userId);
        Film film = validateAndGetFilm(filmId, userId);

        Set<Long> filmLikes = likes.get(filmId);
        if (filmLikes != null) {
            boolean wasRemoved = filmLikes.remove(userId);

            film.getLikes().remove(userId);

            if (wasRemoved) {
                log.info("Лайк удалён: фильм {} потерял лайк от пользователя {}", filmId, userId);

                if (filmLikes.isEmpty()) {
                    likes.remove(filmId);
                    log.debug("Список лайков фильма {} очищен", filmId);
                }
            } else {
                log.warn("Попытка удалить несуществующий лайк: фильм {} от пользователя {}", filmId, userId);
            }
        }

        return film;
    }

    public List<Film> getTheMostPopularFilms(int count) {
        log.debug("Запрос на получение {} популярных фильмов", count);

        List<Film> popularFilms = filmStorage.findAllFilms()
                .stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());

        log.debug("Найдено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    public Film validateAndGetFilm(Long filmId, Long userId) {
        log.trace("Валидация фильма {} и пользователя {}", filmId, userId);
        Film film = validator.getFilmOrThrow(filmId);
        validator.getUserOrThrow(userId);
        return film;
    }
}
