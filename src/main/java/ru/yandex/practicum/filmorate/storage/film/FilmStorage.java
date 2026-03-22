package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> findById(Long id);

    Film delete(Long id);

    // Получить популярные фильмы с фильтрацией по жанру и году
    Collection<Film> getPopular(int count, Long genreId, Integer year);

    Long addLike(Long filmId, Long userId);

    Long removeLike(Long filmId, Long userId);

    boolean contains(Long id);

    Collection<Film> findByDirectorId(Long directorId, String sortBy);

    Collection<Film> search(String query, String by);

    Collection<Film> getCommonFilms(Long userId, Long friendId);

}
