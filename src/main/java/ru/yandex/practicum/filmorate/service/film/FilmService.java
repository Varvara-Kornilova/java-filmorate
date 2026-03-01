package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.validator.EntityValidator;
import java.util.Comparator;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final EntityValidator validator;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            EntityValidator validator,
            MpaStorage mpaStorage,
            GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.validator = validator;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Film update(Film newFilm) {
        if (newFilm.getMpaRatingId() != null) {
            mpaStorage.findById(newFilm.getMpaRatingId())
                    .orElseThrow(() -> new NotFoundException(
                            "Рейтинг с id = " + newFilm.getMpaRatingId() + " не найден"));
        }

        if (newFilm.getGenreIds() != null && !newFilm.getGenreIds().isEmpty()) {
            for (Long genreId : newFilm.getGenreIds()) {
                genreStorage.findById(genreId)
                        .orElseThrow(() -> new NotFoundException(
                                "Жанр с id = " + genreId + " не найден"));
            }
        }

        return filmStorage.update(newFilm);
    }

    public List<Film> getAllFilms() {
        return filmStorage.findAllFilms();
    }

    public Film getFilmById(Long id) {
        return validator.getFilmOrThrow(id);
    }

    public Film create(Film film) {

        if (film.getMpaRatingId() != null) {
            mpaStorage.findById(film.getMpaRatingId())
                    .orElseThrow(() -> new NotFoundException(
                            "Рейтинг с id = " + film.getMpaRatingId() + " не найден"));
        }

        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            for (Long genreId : film.getGenreIds()) {
                genreStorage.findById(genreId)
                        .orElseThrow(() -> new NotFoundException(
                                "Жанр с id = " + genreId + " не найден"));
            }
        }

        return filmStorage.create(film);
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

        if (filmLikes == null) {
            log.warn("Попытка удалить лайк у фильма {}, у которого нет лайков", filmId);
            return film;
        }

        boolean wasRemoved = filmLikes.remove(userId);

        if (!wasRemoved) {
            log.warn("Попытка удалить несуществующий лайк: фильм {} от пользователя {}", filmId, userId);
            return film;
        }

        film.getLikes().remove(userId);
        log.info("Лайк удалён: фильм {} потерял лайк от пользователя {}", filmId, userId);

        if (filmLikes.isEmpty()) {
            likes.remove(filmId);
            log.debug("Список лайков фильма {} очищен", filmId);
        }

        return film;
    }

    public List<Film> getTheMostPopularFilms(int count) {
        log.debug("Запрос на получение {} популярных фильмов", count);

        List<Film> popularFilms = filmStorage.findAllFilms()
                .stream()
                .sorted(Comparator
                        .comparingInt((Film film) -> film.getLikes().size()).reversed()
                        .thenComparing(Film::getId, Comparator.nullsLast(Comparator.naturalOrder()))
                )
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
