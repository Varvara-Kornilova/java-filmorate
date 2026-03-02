package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> findById(Long id);

    // Удаление фильма (если нужно по тестам)
    Film delete(Long id);

    // Популярные фильмы
    Collection<Film> getPopular(int count);

    // Лайки
    Long addLike(Long filmId, Long userId);

    Long removeLike(Long filmId, Long userId);

    public boolean contains(Long id);
}
